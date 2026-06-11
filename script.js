const fs = require('fs');

let code = fs.readFileSync('app/src/main/java/com/example/ui/DashboardScreen.kt', 'utf8');

// Replace standard spans
code = code.replace(/item\(span = StaggeredGridItemSpan\.FullLine\)/g, 'item');
code = code.replace(/item\(span = StaggeredGridItemSpan\.SingleLane\)/g, 'item');

// Update Grid to LazyColumn
code = code.replace('import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid', 'import androidx.compose.foundation.lazy.LazyColumn');
code = code.replace('import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells', '');
code = code.replace('import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan', '');
code = code.replace('import androidx.compose.foundation.lazy.staggeredgrid.items', 'import androidx.compose.foundation.lazy.items');

code = code.replace('LazyVerticalStaggeredGrid(', 'LazyColumn(');
code = code.replace('columns = StaggeredGridCells.Fixed(gridLayoutColumns),', '');
code = code.replace('horizontalArrangement = Arrangement.spacedBy(12.dp),', 'verticalArrangement = Arrangement.spacedBy(16.dp),');

// Wipe items span
code = code.replace(/span = \{ id ->\s*if \([^)]+\) StaggeredGridItemSpan\.FullLine\s*else StaggeredGridItemSpan\.SingleLane\s*\}/g, '');

fs.writeFileSync('app/src/main/java/com/example/ui/DashboardScreen.kt', code);
console.log('Done!');
