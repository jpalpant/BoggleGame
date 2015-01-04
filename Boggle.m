format shortG

lexsize = [5757 19912 45356 80612];
SimpleLexTimes = [0.005 0.010 0.015 0.016;       %iter time
                 0.003 0.003 0.008 0.010;        %word time
                 0.008 0.032 0.027 0.039];        %pref time
             
BinaryLexTimes = [0 0 0.008 0.013;
                  0 0 0 0;
                  0.011 0.08 0.085 0.107];
              
TrieLexTimes   = [0.068 0.162 0.115 0.154;
                  0.002 0 0.001 0.03;
                  0.005 0.013 0.018 0.03];
              
              
              
numTrials = [10 50 100 300 500 1000 10000];
maxWords = [205 410 423 502 502 889 889];

LexiconAutoPlayerTimes = [2.39 11.988 24.61 67.62 172.05 287.22 7121.176;       %Simple Lexicon
                          3.213 17.852 32.786 92.267 172.049 313.814 3227.695;  %Trie Lexicon
                          2.409 12.352 25.187 73.009 161.684 235.656 2471.909]; %Binary Search Lexicon
BoardAutoPlayerTimes =   [0.132 0.583 0.964 0.99 1.381 2.144 15.309;
                          0.099 0.49 0.961 2.124 3.963 3.678 12.79;
                          0.11 0.506 0.993 2.167 4.52 1.752 13.132];
                      

                      
                      
%%Compare lexicons, 3 graphs - iter performance, word performance, pref performance                      
figure(1);
clf;
plot(lexsize, SimpleLexTimes(1,:), 'k-o', lexsize, BinaryLexTimes(1,:), 'b-s', lexsize, TrieLexTimes(1,:), 'r-h');
xlabel('Size of lexicon (words)');
ylabel('Run time of lexicon iteration (s)');
title('Plot of lexicon size vs iteration time from LexiconBenchmark');
legend('SimpleLexicon', 'BinarySearchLexicon', 'TrieLexicon', 0);
print -djpeg -r300 LexIterTimes

                      
figure(2);
clf;
plot(lexsize, SimpleLexTimes(2,:), 'k-o', lexsize, BinaryLexTimes(2,:), 'b-s', lexsize, TrieLexTimes(2,:), 'r-h');
xlabel('Size of lexicon (words)');
ylabel('Run time of word check (s)');
title('Plot of lexicon size vs word check time from LexiconBenchmark');
legend('SimpleLexicon', 'BinarySearchLexicon', 'TrieLexicon', 0);
print -djpeg -r300 LexWordTimes

                      
figure(3);
clf;
plot(lexsize, SimpleLexTimes(3,:), 'k-o', lexsize, BinaryLexTimes(3,:), 'b-s', lexsize, TrieLexTimes(3,:), 'r-h');
xlabel('Size of lexicon (words)');
ylabel('Run time of prefix check (s)');
title('Plot of lexicon size vs prefix check time from LexiconBenchmark');
legend('SimpleLexicon', 'BinarySearchLexicon', 'TrieLexicon', 0);
print -djpeg -r300 LexPrefTimes


%%Compare Autoplayers
figure(4);
clf;
semilogx(numTrials, LexiconAutoPlayerTimes(1, :), 'k-o', numTrials, LexiconAutoPlayerTimes(2, :) , 'b-s', numTrials, LexiconAutoPlayerTimes(3, :), 'r-h');
xlabel('Number of boards generated (semilog scale)');
ylabel('Run time (s)');
title('Runtime of LexiconFirstAutoPlayer for different lexicon types');
legend('SimpleLexicon', 'BinarySearchLexicon', 'TrieLexicon', 0);
print -djpeg -r300 LexFirstAutoPlayer

figure(5);
clf;
semilogx(numTrials, BoardAutoPlayerTimes(1, :), 'k-o', numTrials, BoardAutoPlayerTimes(2, :) , 'b-s', numTrials, BoardAutoPlayerTimes(3, :), 'r-h');
xlabel('Number of boards generated (semilog scale)');
ylabel('Run time (s)');
title('Runtime of BoardFirstAutoPlayer for different lexicon types');
legend('SimpleLexicon', 'BinarySearchLexicon', 'TrieLexicon', 0);
print -djpeg -r300 BoardFirstAutoPlayer


%%Use average irrespective of lexicon - plot full power fit and both values on
%%same graph             
LexiconAutoPlayerAvgs = mean(LexiconAutoPlayerTimes, 1);
BoardAutoPlayerAvgs = mean(BoardAutoPlayerTimes, 1);

range = logspace(log10(min(numTrials)), log10(max(numTrials)), 100);

PowerFit = @(coefs, x) coefs(1) * x.^(coefs(2));

fSSR = @(coefs, x, y) sum((y - PowerFit(coefs, x)).^2);

[LexCoefs, LexSr] = fminsearch(@(VarCoefs) fSSR(VarCoefs, numTrials, LexiconAutoPlayerAvgs), [1, 1]);
[BoardCoefs, BoardSr] = fminsearch(@(VarCoefs) fSSR(VarCoefs, numTrials, BoardAutoPlayerAvgs), [1, 1]);


%Plot Lexicon and power fit
figure(6);
clf;
loglog(numTrials, LexiconAutoPlayerAvgs, 'b-s', range, PowerFit(LexCoefs, range), 'b-.');
%Plot Board and power fit
hold on
loglog(numTrials, BoardAutoPlayerAvgs, 'r-s', range, PowerFit(BoardCoefs, range), 'r-.');
hold off


LexTime = [PowerFit(LexCoefs, 10000), PowerFit(LexCoefs, 1000000)]
BoardTime = [PowerFit(BoardCoefs, 10000), PowerFit(BoardCoefs, 1000000)]

LexLegend = ['Power fit y = ', num2str(LexCoefs(1), '%2.4f'), 'x^{', num2str(LexCoefs(2), '%2.4f'), '} of LexiconFirst'];
BoardLegend = ['Power fit y = ', num2str(BoardCoefs(1), '%2.4f'), 'x^{', num2str(BoardCoefs(2), '%2.4f'), '} of BoardFirst'];
legend('LexiconFirstAutoPlayer average times', LexLegend, 'BoardFirstAutoPlayer average times', BoardLegend, 0);
xlabel('Number of boards generated (log scale)')
ylabel('Run time (s, log scale)');
title({'Comparison of BoardFirstAutoPlayer and LexiconFirstAutoPlayer','With least-squares power fits'});
print -djpeg -r300 AutoPlayerComparison


%%Now, try a power fit with the exponent clamped at 1 - equivalent to a linear fit
%%with the y-intercept clamped at zero
fSSRLinear = @(a, x, y) sum((y - PowerFit([a, 1], x)).^2);
[LexA, LexSr2] = fminsearch(@(avar) fSSRLinear(avar, numTrials, LexiconAutoPlayerAvgs), 1);
[BoardA, BoardSr2] = fminsearch(@(avar) fSSRLinear(avar, numTrials, BoardAutoPlayerAvgs), 1);


%Plot Lexicon and power fit
figure(7);
clf;
loglog(numTrials, LexiconAutoPlayerAvgs, 'b-s', range, PowerFit([LexA,1], range), 'b-.');
%Plot Board and power fit
hold on
loglog(numTrials, BoardAutoPlayerAvgs, 'r-s', range, PowerFit([BoardA, 1], range), 'r-.');
hold off

LexLegend = ['Power fit y = ', num2str(LexA, '%2.4f'), 'x^{1} of LexiconFirst'];
BoardLegend = ['Power fit y = ', num2str(BoardA, '%2.4f'), 'x^{1} of BoardFirst'];
legend('LexiconFirstAutoPlayer average times', LexLegend, 'BoardFirstAutoPlayer average times', BoardLegend, 0);
xlabel('Number of boards generated (log scale)')
ylabel('Run time (s, log scale)');
title({'Comparison of BoardFirstAutoPlayer and LexiconFirstAutoPlayer','Power fits with exponent clamped to 1'});
print -djpeg -r300 LinearAutoPlayerComparison

LexTimeLinear = [PowerFit([LexA,1], 10000), PowerFit([LexA,1], 1000000)]
BoardTimeLinear = [PowerFit([BoardA,1], 10000), PowerFit([BoardA,1], 1000000)]

