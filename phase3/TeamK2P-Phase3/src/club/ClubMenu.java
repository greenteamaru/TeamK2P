package club;

import java.util.Scanner;

public class ClubMenu {

    private final Scanner sc;
    private final ClubService clubService;

    public ClubMenu(Scanner sc) {
        this.sc = sc;
        this.clubService = new ClubService();
    }

    // 일반 사용자용 클럽 메뉴
    public void showUserClubMenu(int userId) {
        while (true) {
            System.out.println("\n===== 클럽 메뉴 =====");
            System.out.println("1. 전체 클럽 목록 보기");
            System.out.println("2. 카테고리로 클럽 검색");
            System.out.println("3. 이름으로 클럽 검색");
            System.out.println("4. 내가 가입한 클럽 보기");
            System.out.println("5. 클럽 가입");
            System.out.println("6. 클럽 탈퇴");
            System.out.println("0. 이전 메뉴로");
            System.out.print("선택 >> ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> clubService.listAllClubs();
                case "2" -> clubService.listClubsByCategory(sc);
                case "3" -> clubService.searchClubsByName(sc);
                case "4" -> clubService.listMyClubs(userId);
                case "5" -> clubService.joinClub(sc, userId);
                case "6" -> clubService.leaveClub(sc, userId);
                case "0" -> {
                    System.out.println("이전 메뉴로 돌아갑니다.");
                    return;
                }
                default -> System.out.println("잘못된 선택입니다.");
            }
        }
    }
}
