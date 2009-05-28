load("base.js");
load("richards.js");
load("deltablue.js");
load("crypto.js");
load("raytrace.js");
load("earley-boyer.js");
load("regexp.js");

var completed = 0;
var benchmarks = BenchmarkSuite.CountBenchmarks();
var success = true;

function ShowProgress(name) {
  var percentage = ((++completed) / benchmarks) * 100;
  print("Running: " + Math.round(percentage) + "% completed.");
}


function AddResult(name, result) {
  var text = name + ': ' + result;
  print(text)
}


function AddError(name, error) {
  AddResult(name, '*error*');
  success = false;
}


function AddScore(score) {
  if (success) {
    print("Score: " + score);
  }
}


function Run() {
  BenchmarkSuite.RunSuites({ NotifyStep: ShowProgress,
                             NotifyError: AddError,
                             NotifyResult: AddResult,
                             NotifyScore: AddScore }); 
}

Run()
