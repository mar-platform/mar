export class ItemHelper {
    static smellSize(item) {
        if (item.metadata == undefined)
            return 0
        var smells = item.metadata.smells
        return Object.keys(smells).length
    }

    static numElements(item) {
        if (item.metadata == undefined)
            return 0
        return item.metadata.numElements
    }

    static topics(item) {
        if (item.metadata == undefined)
            return []
        return item.metadata.topics
    }

    static category(item) {
        if (item.metadata == undefined)
            return null;
        return item.metadata.category;
    }

    static modelType(item) {
        return item.modelType;
    }

    static origin(item) {
        return item.origin;
    }
}

export function intCompare(a, b) {
    if (a > b) return 1;
    if (b > a) return -1;

    return 0;
}