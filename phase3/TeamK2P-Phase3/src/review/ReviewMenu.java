package review;

import java.util.Scanner;

public class ReviewMenu {

    private final Scanner sc;
    private final ReviewService reviewService;

    public ReviewMenu(Scanner sc) {
        this.sc = sc;
        this.reviewService = new ReviewService();
    }

    public void showUserReviewMenu(int userId) {
        while (true) {
            System.out.println("\n===== 리뷰 메뉴 =====");
            System.out.println("1. 내가 쓴 리뷰 목록");
            System.out.println("2. 특정 이벤트 리뷰 보기");
            System.out.println("3. 리뷰 작성");
            System.out.println("4. 리뷰 수정");
            System.out.println("5. 리뷰 삭제");
            System.out.println("0. 이전 메뉴로");
            System.out.print("선택 >> ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> reviewService.listMyReviews(userId);
                case "2" -> reviewService.listReviewsByEvent(sc);
                case "3" -> reviewService.writeReview(sc, userId);
                case "4" -> reviewService.updateMyReview(sc, userId);
                case "5" -> reviewService.deleteMyReview(sc, userId);
                case "0" -> {
                    System.out.println("이전 메뉴로 돌아갑니다.");
                    return;
                }
                default -> System.out.println("잘못된 선택입니다.");
            }
        }
    }
}
