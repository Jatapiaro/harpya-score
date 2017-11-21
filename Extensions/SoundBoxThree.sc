SoundBoxThree{

	var lightSynth, musicBoxBuffer, musicBoxSynth, forksAndKnivesBuffer,
	forksAndKnivesSynth, eatingBuffer, eatingSynth, tablePunchBuffer, tablePunchSynth,
	doruminBuffer, doruminSynth, doruminTwoBuffer, doruminTwoSynth, bellsBuffer,
	bellsSynth, pianoLoopBuffer, pianoLoopSynth;

	var dur, note, r;

	*new{
		^super.new.init;
	}

	init{
		musicBoxBuffer = Buffer.read(
			Server.default, Platform.resourceDir +/+"sounds/music-box.wav"
		);
		forksAndKnivesBuffer = Buffer.read(
			Server.default, Platform.resourceDir +/+"sounds/forks-and-knives.wav"
		);
		eatingBuffer = Buffer.read(
			Server.default, Platform.resourceDir +/+"sounds/eating.wav"
		);
		tablePunchBuffer = Buffer.read(
			Server.default, Platform.resourceDir +/+"sounds/table-punch.wav"
		);
		doruminBuffer = Buffer.read(
			Server.default, Platform.resourceDir +/+"sounds/dorumin.wav"
		);
		doruminTwoBuffer = Buffer.read(
			Server.default, Platform.resourceDir +/+"sounds/dorumin-2.wav"
		);
		bellsBuffer = Buffer.read(
			Server.default, Platform.resourceDir +/+"sounds/church-bells.wav"
		);
		pianoLoopBuffer = Buffer.read(
			Server.default, Platform.resourceDir +/+"sounds/piano-rayado.wav"
		);

		this.makeAllSynthDefs;
		this.showSoundBoxTwoGUI;
				dur = {|len1=1, len2=2|
			Plazy({
				var dur = len1;

				inf.do{|n|
					var overlap = (n % len1 == 0) || (n % len2 == 0);
					if(n>=len1 && dur==len1 && overlap,
						{
							dur=len2;
						},
						{
							if(n>0 && dur==len2 && overlap,
								{
									dur=len1;
							});
						}
					);
					dur.yield;
				};
			}).asStream;
		};

		note = { |root=48|
			Plazy({
				var scale = Scale.chromatic.degrees.flat;
				inf.do{|n|
					var oct = (n / scale.size).floor;
					var degree = 12*oct + scale.wrapAt(n) + root;
					degree.yield;
				}
			}).asStream;
		};

		r = Routine({
			var durStream  = dur.value(9, 14.5);
			var noteStream = note.value(48);
			inf.do {
				var dur  = durStream.next();
				var note = noteStream.next();
				fork{
					inf.do{
						Synth(\piano, [\midinote, note]);
						2.yield;
					};
				};
				dur.yield;
			};
		});
	}

	showSoundBoxTwoGUI {

		var window, lightSynthButton, musicBoxButton, forksAndKnivesButton,
		eatingButton, tablePunchButton, doruminButton, doruminTwoButton, pianoButton,
		bellsButton, pianoLoopButton;

		window = Window.new(
			"Sound Box Three - Window", Rect(width: 500, height: 500)).front;

		lightSynthButton = Button(window, Rect(20, 20, 75, 50))
		.states_(
			[
				["Lights", Color.black, Color.green],
				["Stop Lights", Color.black, Color.red]
			]
		).action_({|bt|
			this.playLight(1);
		});

		musicBoxButton = Button(window, Rect(100, 20, 75, 50))
		.states_(
			[
				["Music Box", Color.black, Color.green],
				["Stop Music Box", Color.black, Color.red]
			]
		).action_({|bt|
			this.playMusicBox;
		});

		forksAndKnivesButton = Button(window, Rect(180, 20, 75, 50))
		.states_(
			[
				["Forks & Knives", Color.black, Color.green],
				["Stop Forks & Knives", Color.black, Color.red]
			]
		).action_({|bt|
			this.playForksAndKnives;
		});

		eatingButton = Button(window, Rect(260, 20, 75, 50))
		.states_(
			[
				["Eating", Color.black, Color.green],
				["Stop Eating", Color.black, Color.red]
			]
		).action_({|bt|
			this.playEating;
		});

		tablePunchButton = Button(window, Rect(340, 20, 75, 50))
		.states_(
			[
				["Table Punch", Color.black, Color.green],
				["Stop Table Punch", Color.black, Color.red]
			]
		).action_({|bt|
			this.playTablePunch;
		});

		doruminButton = Button(window, Rect(20, 80, 75, 50))
		.states_(
			[
				["Dorumin", Color.black, Color.green],
				["Stop Dorumin", Color.black, Color.red]
			]
		).action_({|bt|
			this.playDorumin;
		});

		doruminTwoButton = Button(window, Rect(100, 80, 75, 50))
		.states_(
			[
				["Dorumin 2", Color.black, Color.green],
				["Stop Dorumin 2", Color.black, Color.red]
			]
		).action_({|bt|
			this.playDoruminTwo;
		});

		pianoButton = Button(window, Rect(180, 80, 75, 50))
		.states_(
			[
				["Piano", Color.black, Color.red]
			]
		).action_({|bt|
			this.playPiano;
		});

		bellsButton = Button(window, Rect(260, 80, 75, 50))
		.states_(
			[
				["Bells", Color.black, Color.green],
				["Stop Bells", Color.black, Color.red]
			]
		).action_({|bt|
			this.playBells;
		});

		pianoLoopButton = Button(window, Rect(340, 80, 75, 50))
		.states_(
			[
				["Piano Loop", Color.black, Color.green],
			]
		).action_({|bt|
			this.playPianoLoop;
		});

	}

	playLight{ arg amp;

		if ( lightSynth == nil,
			{
				lightSynth = Synth(\lights, [amp: amp]);
			},
			{
				lightSynth.free;
				lightSynth = nil;
			}
		);

	}

	makeAllSynthDefs {

		SynthDef(\lights, { | amp = 1 |

			var humSource, noise, comb;
			humSource = Clip.ar(LFSaw.ar([99.8, 100.2], 1, 0.5, 0.5).sum - 1, -0.5, 0.5);
			noise = LPF.ar(LPF.ar(WhiteNoise.ar,2),2);
			noise = noise * noise * 500;

			humSource = humSource * noise;

			comb = DelayC.ar(InFeedback.ar(10), delaytime: (noise+20)/1000);
			OffsetOut.ar(
				10, (humSource + OnePole.ar(comb * 0.2, exp(-2pi * (3000 * SampleDur.ir))))
			);
			Out.ar ( 0, comb!2  * amp);
		}).add();

		SynthDef(\sound, {

			arg gate = 1, amp = 0.7, buffer = nil;

			var env, play;

			env = EnvGen.kr(Env.asr(3,1,1), gate, doneAction: 2);

			play = PlayBuf.ar(2, buffer, 1, loop: 1);

			Out.ar(0, play*env*amp);

		}).add();

		SynthDef(\sound2, {

			arg gate = 1, amp = 0.7, buffer = nil;

			var env, play;

			env = EnvGen.kr(Env.asr(3,1,1), gate, doneAction: 2);

			play = PlayBuf.ar(2, buffer, 1, loop: 0);

			Out.ar(0, play*env*amp);

		}).add();

		SynthDef(\piano, { |midinote, outBus=0, gate = 0|
			var env = EnvGen.kr( Env.asr(1,0.1,0.1) , gate, doneAction:2);
			Out.ar(outBus, MdaPiano.ar(midinote.midicps, decay:0.5));
		}).add();

	}

	playMusicBox {
		if ( musicBoxSynth == nil,
			{
				musicBoxSynth = Synth(\sound, [buffer: musicBoxBuffer]);
			},
			{
				musicBoxSynth.set(\gate, 0);
				musicBoxSynth = nil;
			}
		);
	}

	playForksAndKnives {
		if ( forksAndKnivesSynth == nil,
			{
				forksAndKnivesSynth = Synth(\sound, [buffer: forksAndKnivesBuffer]);
			},
			{
				forksAndKnivesSynth.set(\gate, 0);
				forksAndKnivesSynth = nil;
			}
		);
	}

	playEating {
		if ( eatingSynth == nil,
			{
				eatingSynth = Synth(\sound, [buffer: eatingBuffer]);
			},
			{
				eatingSynth.set(\gate, 0);
				eatingSynth = nil;
			}
		);
	}

	playBells {
		if (bellsSynth == nil,
			{
				bellsSynth = Synth(\sound, [buffer: bellsBuffer]);
			},
			{
				bellsSynth.set(\gate, 0);
				bellsSynth = nil;
			}
		);
	}

	playTablePunch {
		if ( tablePunchSynth == nil,
			{
				tablePunchSynth = Synth(\sound2, [
					buffer: tablePunchBuffer,
					amp: 10
				]);
			},
			{
				tablePunchSynth.set(\gate, 0);
				tablePunchSynth = nil;
			}
		);
	}

	playDorumin {
		if ( doruminSynth == nil,
			{
				doruminSynth = Synth(\sound, [buffer: doruminBuffer,]);
			},
			{
				doruminSynth.set(\gate, 0);
				doruminSynth = nil;
			}
		);
	}

	playDoruminTwo {
		if ( doruminTwoSynth == nil,
			{
				doruminTwoSynth = Synth(\sound, [buffer: doruminTwoBuffer,]);
			},
			{
				doruminTwoSynth.set(\gate, 0);
				doruminTwoSynth = nil;
			}
		);
	}

	playPiano {
		r.play(TempoClock(3));
	}

	playPianoLoop {
		if ( pianoLoopSynth == nil,
			{
				pianoLoopSynth = Synth(\sound2, [
					buffer: pianoLoopBuffer,
				]);
			},
			{
				pianoLoopSynth.set(\gate, 0);
				pianoLoopSynth = nil;
			}
		);
	}
}