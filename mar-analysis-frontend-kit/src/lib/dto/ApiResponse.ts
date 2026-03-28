export default interface ApiResponse<T> {
    status: number;
    data: T | null;
}
