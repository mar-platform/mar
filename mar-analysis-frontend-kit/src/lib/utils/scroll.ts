export function scrollToDetails() {
    const details = document.getElementById('details');
    if (details) {
        details.scrollIntoView({ 
            behavior: 'smooth',
            block: 'start'
        });
    }
}

export function scrollToGraph() {
    const graphView = document.getElementById('graph-view');
    if (graphView) {
        graphView.scrollIntoView({ 
            behavior: 'smooth',
            block: 'start'
        });
    }
}
