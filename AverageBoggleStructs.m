function [s] = AverageBoggleStructs(DataStructs)
%Takes several Boggle Data Structs and averages the key values (time,
%score, accepted, scored) for each grid point
%Returns as single Boggle Data Struct

numStructs = length(DataStructs);

s = DataStructs(1);
if numStructs == 1
    return
end

s.Times  = (s.Times / numStructs) .* ~s.OutliersGrid;
s.Scores = s.Scores / numStructs;
s.NumAccepted = s.NumAccepted / numStructs;
s.NumScored = s.NumScored / numStructs;
OutliersCount = ones(size(s.OutliersGrid)) .* s.OutliersGrid;

for s2index = 2:numStructs
    s2 = DataStructs(s2index);
    if(~isequal(fieldnames(s), fieldnames(s2)))
        error('The structures do not have the same fields.  Function aborted');
    end
    
    %Compare Param1Grid and Param2Grid
    if(~all(s.Param1Grid(:) == s2.Param1Grid(:)))
        err = ['Param1Grid does not align in structs 1 and ', num2str(s2index)];
        error(err);
    end
    
    if(~all(s.Param2Grid(:) == s2.Param2Grid(:)))
        err = ['Param2Grid does not align in structs 1 and ', num2str(s2index)];
        error(err);
    end
    
    s.Scores = s.Scores + s2.Scores / numStructs;
    s.Times = s.Times + ((s2.Times / numStructs) .* ~s2.OutliersGrid);
    s.NumAccepted = s.NumAccepted + s2.NumAccepted/ numStructs;
    s.NumScored = s.NumScored  + s2.NumScored / numStructs;
    OutliersCount = OutliersCount + (ones(size(s2.OutliersGrid)) .* s2.OutliersGrid);
end

%Correct for the number of outliers counted at each location
s.Times = s.Times .* (numStructs ./ (numStructs - OutliersCount));

