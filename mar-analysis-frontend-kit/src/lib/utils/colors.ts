function stringToHue(str: string): number {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return Math.abs(hash) % 360;
}

export function getConsistentColor(text: string): string {
    const hue = stringToHue(text);
    return `hsl(${hue}, 70%, 80%)`;
}

export function getColorByIndex(index: number): string {
    const hue = (index * 137.508) % 360;
    return `hsl(${hue.toFixed(1)}, 70%, 80%)`;
}
