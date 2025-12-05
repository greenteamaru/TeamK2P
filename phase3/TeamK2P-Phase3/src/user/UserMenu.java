package user;

import event.EventMenu;
import review.ReviewMenu;
import club.ClubMenu;

import java.util.Scanner;

public class UserMenu {

    private final Scanner sc;
    private final UserService userService;
    private final EventMenu eventMenu;
    private final ReviewMenu reviewMenu;
    private final ClubMenu clubMenu;
    

    public UserMenu(Scanner sc) {
        this.sc = sc;
        this.userService = new UserService();
        this.eventMenu = new EventMenu(sc);
        this.reviewMenu = new ReviewMenu(sc);
        this.clubMenu = new ClubMenu(sc);
    }

    public void signUp() {
        userService.signUp(sc);
    }

    public void login() {
        Integer userId = userService.login(sc);

        if (userId == null) {
            // 로그인 실패
            return;
        }

        boolean isAdmin = userService.isAdmin(userId);

        if (isAdmin) {
            adminMenu(userId);
        } else {
            userMainMenu(userId);
        }
    }

 // 일반 사용자 메뉴
    private void userMainMenu(int userId) {
        while (true) {
            System.out.println("\n===== 사용자 메뉴 =====");
            System.out.println("1. 내 정보 조회");
            System.out.println("2. 내 정보 수정");
            System.out.println("3. 비밀번호 변경");
            System.out.println("4. 회원 탈퇴");
            System.out.println("5. 이벤트 기능");
            System.out.println("6. 클럽 기능");
            System.out.println("7. 리뷰 기능");
            System.out.println("0. 로그아웃");
            System.out.print("선택 >> ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> userService.viewProfile(userId);
                case "2" -> userService.updateProfile(sc, userId);
                case "3" -> userService.changePassword(sc, userId);
                case "4" -> {
                    userService.deleteAccount(userId);
                    System.out.println("메인 메뉴로 돌아갑니다.");
                    return;
                }
                case "5" -> eventMenu.showUserEventMenu(userId);
                case "6" -> clubMenu.showUserClubMenu(userId);   // ← 클럽 메뉴 호출
                case "7" -> reviewMenu.showUserReviewMenu(userId);
                case "0" -> {
                    System.out.println("로그아웃합니다.");
                    return;
                }
                default -> System.out.println("잘못된 선택입니다.");
            }
        }
    }


    // 관리자 메뉴 (이벤트 관리 기능은 나중에 필요하면 추가)
    private void adminMenu(int userId) {
        while (true) {
            System.out.println("\n===== 관리자 메뉴 =====");
            System.out.println("1. 내 정보 조회");
            System.out.println("2. 비밀번호 변경");
            System.out.println("3. 전체 회원 조회");
            System.out.println("4. 이메일로 회원 검색");
            System.out.println("0. 로그아웃");
            System.out.print("선택 >> ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> userService.viewProfile(userId);
                case "2" -> userService.changePassword(sc, userId);
                case "3" -> userService.viewAllUsers();
                case "4" -> userService.findUserByEmail(sc);
                case "0" -> {
                    System.out.println("로그아웃합니다.");
                    return;
                }
                default -> System.out.println("잘못된 선택입니다.");
            }
        }
    }
}
