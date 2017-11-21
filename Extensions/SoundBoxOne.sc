SoundBoxOne{

	var windBells, controledWindBells, controledWindBellsAmp;

	*new{
		^super.new.init;
	}

	init{
		controledWindBellsAmp = 0;
		this.makeAllSynthDefs;
		this.showSoundBoxOneGUI;
	}

	showSoundBoxOneGUI {

		var window, windBellsButton, distantStepButton, medianDistanceStepButton,
		closerStepButton, controledWindBellsButton, controledWindBellsSlider;

		window = Window.new("Sound Box One - Window", Rect(width: 500, height: 500)).front;

		//
		windBellsButton = Button(window, Rect(20, 50, 200, 20))
		.states_(
			[
				["1. Play / ( Start Ambien Music )", Color.black, Color.green],
				["1. Stop / ( Start Ambien Music )", Color.black, Color.red]
			]
		).action_({|bt|
			var val;
			val = bt.value();
			this.startOrStopWindBells();

		});


		//
		distantStepButton = Button(window, Rect(20, 80, 75, 50))
		.states_(
			[
				["Distant Step", Color.black, Color.green]
			]
		).action_({|bt|
			this.playStep(5);
		});

		distantStepButton = Button(window, Rect(100, 80, 75, 50))
		.states_(
			[
				["MediumD Step", Color.black, Color.green]
			]
		).action_({|bt|
			this.playStep(10);
		});

		closerStepButton = Button(window, Rect(180, 80, 75, 50))
		.states_(
			[
				["Closer Step", Color.black, Color.green]
			]
		).action_({|bt|
			this.playStep(20);
		});


		controledWindBellsButton = Button(window, Rect(20, 150, 200, 20))
		.states_(
			[
				["2. Play / ( 2nd Hungry Music )", Color.black, Color.green],
				["2. Stop / ( 2nd Hungry Music )", Color.black, Color.red]
			]
		).action_({|bt|
			var val;
			val = bt.value();
			this.turnOnControlledWindBells();

		});

		controledWindBellsSlider = Slider(window, Rect(230,150,200,20))
		.value_(controledWindBellsAmp)
		.step_(0.2)
		.action_({|sl|
			var val;
			val = sl.value();
			val.postln;
			controledWindBells.set(\amp,val);
		});

	}

	startOrStopWindBells {

		if ( windBells == nil,
			{
				windBells = Synth(\windBells);
			},
			{
				windBells.free;
				windBells = nil;
				this.recompileWindBells;
			}
		);

	}

	turnOnControlledWindBells {

		if ( controledWindBells == nil,
			{
				this.recompileWindBells();
				controledWindBells = Synth(\windBells, [amp: controledWindBellsAmp]);
			},
			{
				controledWindBells.free;
				controledWindBells = nil;
			}
		);

	}

	playStep { arg distance;
		var step = Synth(\step, [amp: distance]);
		var d = distance;
		d.postln;
	}

	makeAllSynthDefs {

		this.recompileWindBells;

		SynthDef(\step, {
			var sin, env;
			sin = DC.ar(0);
			sin = sin + (SinOsc.ar(XLine.ar(800, 400, 0.01)) * Env.perc(0.0005, 0.01).ar);
			sin = sin + (
				BPF.ar(Hasher.ar(Sweep.ar), XLine.ar(800, 100, 0.01), 0.6) *
				Env.perc(0.001, 0.02).delay(0.001).ar
			);
			sin = sin + (
				SinOsc.ar(XLine.ar(172, 50, 0.01)) *
				Env.perc(0.0001, 0.3, 1, \lin).delay(0.005).ar(2)
			);
			sin = sin.tanh;
			Out.ar(\out.kr(0), Pan2.ar(sin, \pan.kr(0), \amp.kr(0.1)));
		}).add;

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

}