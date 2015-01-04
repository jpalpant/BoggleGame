function [BoggleStruct] = BoggleFileToStruct(filename)

s = tdfread(filename, 'tab');
[Param1Grid, Param2Grid] = meshgrid(unique(s.Param1), unique(s.Param2));

ScoreGrid = zeros(size(Param1Grid));
TimeGrid = zeros(size(Param1Grid));
AcceptedGrid = zeros(size(Param1Grid));
ScoredGrid = zeros(size(Param1Grid));
%BoardsGrid = cell(zeros(size(Param1Grid)));
cellBoards = cellstr(s.Board);

for k = 1:length(s.Param1)
    y = ceil(find(Param1Grid == s.Param1(k))/length(unique(s.Param1)));
    x = mod(find(Param2Grid == s.Param2(k)), length(unique(s.Param2))+1);
    y = y(1);
    x = x(1);
    ScoreGrid(x, y) = s.Score(k);
    TimeGrid(x, y) = s.Time(k);
    AcceptedGrid(x, y) = s.Boards_Accepted(k);
    ScoredGrid(x, y) = s.Boards_Scored(k);
    BoardsGrid{x, y} = cellBoards{k};
end

OutliersGrid = TimeGrid > prctile(s.Time, 98);

BoggleStruct = struct('Param1Grid', Param1Grid, ...
    'Param2Grid', Param2Grid, ...
    'Scores', ScoreGrid,...
    'Times', TimeGrid, ...
    'NumAccepted', AcceptedGrid, ...
    'NumScored', ScoredGrid, ...
    'OutliersGrid', OutliersGrid);

BoggleStruct.Boards = BoardsGrid;

