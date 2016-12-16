# raid_space
an adversarial game agent for a chess game

The chessboard is somewhere between 2\*2 to 26\*26.
There are two possible actions in one move. One is stack, which means a chess could occpies a space on the board. Another action is raid, which occpies the space and turns down the color of the space next to it. 

I used two kind of strategies, min-max and alpha-beta prune. For the optimal version, it should be able to solve a 16\*16 board in 500ms within next 6 steps.