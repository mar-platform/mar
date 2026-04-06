import { globalState } from "$lib/stores/globalState.svelte";

export function downloadGraph(): void {
	const container = document.querySelector('#graph-view');
	if (!container) return;

    // IMPORTANT: Force Sigma to render the WebGL canvas synchronously right now.
    // This fills the WebGL buffer so it isn't blank when we capture it.
    globalState.refreshGraph();

	// Get all the layers (canvases) that Sigma is using
	const canvases = container.querySelectorAll('canvas');
	if (canvases.length === 0) return;

	// Create a temporary canvas to merge all the layers
	const tempCanvas = document.createElement('canvas');
	const ctx = tempCanvas.getContext('2d');
	if (!ctx) return;

	// Use the screen dimensions (actual canvas resolution)
	tempCanvas.width = canvases[0].width;
	tempCanvas.height = canvases[0].height;

	// Paint the background of the temporary canvas with the page foreground color (which is the background color of the graph)
	const backgroundColor = getComputedStyle(document.documentElement).getPropertyValue("--color-page-foreground");
	ctx.fillStyle = backgroundColor;
	ctx.fillRect(0, 0, tempCanvas.width, tempCanvas.height);

	// Draw each Sigma canvas layer onto the temporary canvas in order
	canvases.forEach((layerCanvas) => {
		ctx.drawImage(layerCanvas, 0, 0);
	});

	// Generate the image and trigger the download
	const dataURL = tempCanvas.toDataURL('image/png');
	const downloadLink = document.createElement('a');
	downloadLink.href = dataURL;
	downloadLink.download = 'graph-screenshot.png';

	// Simulate a click to download
	document.body.appendChild(downloadLink);
	downloadLink.click();
	document.body.removeChild(downloadLink);
}
