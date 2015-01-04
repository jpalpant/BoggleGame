clear



depthNum = input('How many Depth Search files are available? ');
annealNum = input('How many Simulated Annealing files are available? ');

runNum = 1;

for i = 1:depthNum
    depthFileName = ['depth', num2str(i), '.txt']
    depthStructs(i) = BoggleFileToStruct(depthFileName);
    bestRuns(runNum) = FindBestRun(depthStructs(i));
    runNum = runNum + 1;
end

for i = 1:annealNum
    annealFileName = ['anneal', num2str(i), '.txt']
    annealStructs(i) = BoggleFileToStruct(annealFileName); 
    bestRuns(runNum) = FindBestRun(annealStructs(i));
    runNum = runNum + 1;
end

annealAvg = AverageBoggleStructs(annealStructs);
depthAvg = AverageBoggleStructs(depthStructs);

figure(1); clf;
surfc(annealAvg.Param1Grid, annealAvg.Param2Grid, annealAvg.Times);
xlabel('Starting Temperature')
ylabel('Failures Allowed')
zlabel('Time (s)')
title('Simulated Annealing Runtime')


figure(2); clf;
surfc(depthAvg.Param1Grid, depthAvg.Param2Grid, depthAvg.Times);
xlabel('Depth')
ylabel('Failures Allowed')
zlabel('Time (s)')
title('Gradient Descent Runtime')


figure(3); clf;
surfc(annealAvg.Param1Grid, annealAvg.Param2Grid, annealAvg.Scores);
xlabel('Starting Temperature')
ylabel('Failures Allowed')
zlabel('Number of Words')
title('Simulated Annealing Best Scores')


figure(4); clf;
surfc(depthAvg.Param1Grid, depthAvg.Param2Grid, depthAvg.Scores);
xlabel('Depth')
ylabel('Failures Allowed')
zlabel('Number of Words')
title('Gradient Descent Best Scores')



