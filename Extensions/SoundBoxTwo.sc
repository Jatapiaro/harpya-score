SoundBoxTwo{

	var violinVolume;

	*new{
		^super.new.init;
	}

	init{
		violinVolume = 0;
		this.makeAllSynthDefs;
		this.showSoundBoxTwoGUI;
	}

	showSoundBoxTwoGUI {

		var window, punchButton, clickButton, handWalkingButton, waterButton,
		startViolinsButton, secondViolinsSoundButton, thirdViolinsSoundButton,
		fourthViolinsSoundButton, fifthViolinsSoundButton;

		window = Window.new("Sound Box Two - Window", Rect(width: 500, height: 500)).front;

		punchButton = Button(window, Rect(20, 20, 75, 50))
		.states_(
			[
				["Punch", Color.black, Color.green]
			]
		).action_({|bt|
			this.playPunch( 10 );
		});

		handWalkingButton = Button(window, Rect(100, 20, 75, 50))
		.states_(
			[
				["Hand Walk", Color.black, Color.green]
			]
		).action_({|bt|
			this.handWalk( 10 );
		});

		clickButton = Button(window, Rect(180, 20, 75, 50))
		.states_(
			[
				["Click Sound", Color.black, Color.green]
			]
		).action_({|bt|
			this.playClick( 10 );
		});

		clickButton = Button(window, Rect(260, 20, 75, 50))
		.states_(
			[
				["Water Sound", Color.black, Color.green]
			]
		).action_({|bt|
			this.playWater( 10, 15, 24);
		});

		startViolinsButton = Button(window, Rect(20, 80, 75, 50))
		.states_(
			[
				["Start Violins", Color.black, Color.green]
			]
		).action_({|bt|
			this.startViolins;
		});

		secondViolinsSoundButton = Button(window, Rect(100, 80, 75, 50))
		.states_(
			[
				["2nd Violins", Color.black, Color.green]
			]
		).action_({|bt|
			this.secondViolinsSound;
		});

		thirdViolinsSoundButton = Button(window, Rect(180, 80, 75, 50))
		.states_(
			[
				["3th Violins", Color.black, Color.green]
			]
		).action_({|bt|
			this.thirdViolinsSound;
		});

		fourthViolinsSoundButton = Button(window, Rect(260, 80, 75, 50))
		.states_(
			[
				["4th Violins", Color.black, Color.green]
			]
		).action_({|bt|
			this.fourthViolinsSound;
		});

		fifthViolinsSoundButton = Button(window, Rect(340, 80, 75, 50))
		.states_(
			[
				["5th Violins", Color.black, Color.green]
			]
		).action_({|bt|
			this.fifthViolinsSound;
		});

	}

	playWater { arg amp, durA, durB;
		Synth(\punch, [amp: amp, durA: durA, durB: durB]);
	}

	playPunch { arg amp;
		Synth(\punch, [amp: amp]);
	}

	playClick { arg amp;
		Synth(\click, [amp: amp]);
	}

	handWalk { arg amp;
		Synth(\handWalking, [amp: amp]);
	}

	makeAllSynthDefs {

		this.recompileWindBells;
		this.recompileViolinsNDef;

		SynthDef(\punch, { | amp = 1, durA = 0.25, durB = 1|
			var amp_env, cut_freq, sin;
			cut_freq = 3000;
			amp_env = EnvGen.ar(Env.perc(durA, durB), doneAction:2);
			sin = LPF.ar( {WhiteNoise.ar(WhiteNoise.ar)}.dup * amp_env, cut_freq ) * amp;
			Out.ar(0, sin);
		}).add;

		SynthDef(\click, {
			var sin;
			sin = DC.ar(0);
			sin = sin + (HPF.ar(Hasher.ar(Sweep.ar), 1320) * Env.perc(0.01, 0.03).ar * 0.5);
			sin = sin + (SinOsc.ar(XLine.ar(750, 161, 0.02)) * Env.perc(0.0005, 0.02).ar);
			sin = sin + (SinOsc.ar(XLine.ar(167, 52, 0.04)) * Env.perc(0.0005, 0.3).ar(2));
			sin = sin.tanh;
			Out.ar(\out.kr(0), Pan2.ar(sin, \pan.kr(0), \amp.kr(0.1)));
		}).add;

		SynthDef(\handWalking, {
			var sin;
			sin = DC.ar(0);
			sin = sin + (
				SinOsc.ar(XLine.ar(1500, 800, 0.01)) * Env.perc(0.0005, 0.01, curve: \lin).ar()
			);
			sin = sin + (
				BPF.ar(Impulse.ar(0) * SampleRate.ir / 48000, 6100, 1.0) * 3.dbamp

			);
			sin = sin + (
				BPF.ar(Hasher.ar(Sweep.ar), 300, 0.9) * Env.perc(0.001, 0.02).ar()
			);
			sin = sin + (
				SinOsc.ar(XLine.ar(472, 60, 0.045)) *
				Env.perc(0.0001, 0.3, curve: \lin).delay(0.005).ar(2)
			);
			sin = sin.tanh;
			Out.ar(\out.kr(0), Pan2.ar(sin, \pan.kr(0), \amp.kr(0.1)));
		}).add();

	}

	recompileWindBells {

		/*
		* This has a different sound each time is added
		*/
		SynthDef(\windBells, { | amp = 1 |
			var n = PinkNoise.ar() * amp ;
			var f = (0.01, 0.015..0.07) * amp;
			var v = LFTri.kr(f.scramble[5..7]).range * amp;
			var sound = Splay.ar( MembraneHexagon.ar(n, f.scramble[1..3], mul: v),
				SinOsc.kr(f.choose));
			Out.ar( 0, sound );
		}).add();

	}

	recompileViolinsNDef {

		Ndef(\violins, {
			var src, freq = 25;
			var lagTime = \lagTime.kr(0);
			var vibrato = \vibrato.kr(
				[
					1.3515662384033, 1.3061940193176, 0.604791264534, 1.2328997278214,
					0.33569867610931, 0.40528120040894, 1.0865307760239,
					1.22394572258, 1.7781081724167, 0.15319232463837
				], lagTime
			);

			var vibRatio = \vibRatio.kr(
				[
					2.9047239411079, 0.39578121743921, 0.10932490532285,
					0.051738858660565, 0.17840235868831, 0.024495380749894,
					3.9689920754869, 4.1139229659643, 0.23327501819885,
					0.97643604193256
				], lagTime
			);

			var freqVary = \freqVary.kr([
				143.76541377614, 14.25479545154, 6.6728691888342, 58.434807398325,
				154.89385962072, 34.112274650688, 3.1140823758236, 10.42040949808,
				158.59959972037, 1.2163188440936 ], lagTime
			);


			var numSrcs = vibrato.size;

			src = numSrcs.collect{|i|
				var freq2 = (freq * freqVary[i]) * LFNoise2.kr(0.01).range(1, 1.5);

				SyncSaw.ar(
					freq2, freq2 *
					SinOsc.ar(Line.kr(1, vibrato[i], 20) * vibRatio[i], Rand(0, 0.8)).range(pi, pi * 0.1)
				) * rrand(0.05, 0.1)
			};

			src = Splay.ar(src);

		});

	}

	startViolins {
		if ( violinVolume == 0,
			{
				violinVolume = 1;
				Ndef(\violins).play();
			},
			{
				violinVolume = 0;
				this.recompileViolinsNDef;
				Ndef(\violins).stop();
			}
		)
	}

	secondViolinsSound {
		var step = "Second violin";
		step.postln;
		Ndef(\violins).xsetn(
			\lagTime, [100],
			\vibRatio, {exprand(0.1, 3)}!10,
			\freqVary, {exprand(1, 150)}!10,
			\vibrato, {exprand(0.3, 3)}!10
		);
	}

	thirdViolinsSound {
		var step = "Third violin";
		step.postln;
		Ndef(\violins).setn(
			\lagTime, [200],
			\vibRatio, ({exprand(0.1, 3)}!10).sort,
			\freqVary, ({exprand(1, 150)}!10).sort,
			\vibrato, ({exprand(0.3, 3)}!10).sort
		);
	}

	fourthViolinsSound {
		var step = "Fourth violin";
		step.postln;
		Ndef(\violins).rebuild;
	}

	fifthViolinsSound {
		var step = "Fifth violin";
		step.postln;
		Ndef(\violins).setn(
			\lagTime, [5],
			\vibRatio, {exprand(0.1, 3)}!10,
			\freqVary, {exprand(1, 150)}!10,
			\vibrato, {exprand(0.3, 3)}!10
		);
	}


}