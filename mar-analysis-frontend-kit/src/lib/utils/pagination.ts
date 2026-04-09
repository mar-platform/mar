
export interface PaginationResult<T> {
  data: T[];
  hasMore: boolean;
  total: number;
}

export function paginateArray<T>(
  items: T[],
  page: number,
  pageSize: number
): PaginationResult<T> {
  // Verify page is at least 1
  const currentPage = Math.max(1, page);
  
  // Calculate start and end indices for slicing the array
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = startIndex + pageSize;

  // Extract the paginated items
  const paginatedItems = items.slice(startIndex, endIndex);

  // Check if there are more items after this slice
  const hasMore = endIndex < items.length;

  return {
    data: paginatedItems,
    hasMore: hasMore,
    total: items.length
  };
}
