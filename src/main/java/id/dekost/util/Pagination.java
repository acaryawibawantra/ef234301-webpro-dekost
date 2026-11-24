package id.dekost.util;

public final class Pagination {

    public static final int DEFAULT_PAGE_SIZE = 10;

    private final int page;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;
    private final long offset;

    private Pagination(int page, int pageSize, long totalItems, int totalPages, long offset) {
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.offset = offset;
    }

    public static Pagination of(String requestedPage, long totalItems) {
        return of(parsePage(requestedPage), totalItems, DEFAULT_PAGE_SIZE);
    }

    public static Pagination of(int requestedPage, long totalItems, int pageSize) {
        if (totalItems < 0) {
            throw new IllegalArgumentException("totalItems tidak boleh negatif");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize harus lebih besar dari 0");
        }

        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int page = totalPages == 0 ? 1 : Math.max(1, Math.min(requestedPage, totalPages));
        long offset = (long) (page - 1) * pageSize;
        return new Pagination(page, pageSize, totalItems, totalPages, offset);
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getOffset() {
        return offset;
    }

    public long getStartItem() {
        return totalItems == 0 ? 0 : offset + 1;
    }

    public long getEndItem() {
        return Math.min(offset + pageSize, totalItems);
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < totalPages;
    }

    private static int parsePage(String requestedPage) {
        if (requestedPage == null || requestedPage.isBlank()) {
            return 1;
        }
        try {
            return Integer.parseInt(requestedPage.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
