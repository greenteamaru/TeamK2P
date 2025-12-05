import user.UserMenu;

import java.util.Scanner;

public class MainMenu {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        UserMenu userMenu = new UserMenu(sc);

        while (true) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. 로그인");
            System.out.println("2. 회원가입");
            System.out.println("0. 종료");
            System.out.print("선택 >> ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> userMenu.login();
                case "2" -> userMenu.signUp();
                case "0" -> {
                    System.out.println("프로그램을 종료합니다.");
                    return;
                }
                default -> System.out.println("잘못된 선택입니다.");
            }
        }
    }
}
