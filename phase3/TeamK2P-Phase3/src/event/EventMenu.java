package event;

import java.util.Scanner;

public class EventMenu {

    private final Scanner sc;
    private final EventService eventService;

    public EventMenu(Scanner sc) {
        this.sc = sc;
        this.eventService = new EventService();
    }

    // 일반 사용자용 이벤트 메뉴
    public void showUserEventMenu(int userId) {
        while (true) {
            System.out.println("\n===== 이벤트 메뉴 =====");
            System.out.println("1. 다가오는 이벤트 보기");
            System.out.println("2. 캠퍼스별 이벤트 보기");
            System.out.println("3. 이벤트 상세 보기");
            System.out.println("4. 내 신청 목록 보기");
            System.out.println("5. 이벤트 신청");
            System.out.println("6. 신청 취소");
            System.out.println("0. 이전 메뉴로");
            System.out.print("선택 >> ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> eventService.listUpcomingEvents();
                case "2" -> eventService.listEventsByCampus(sc);
                case "3" -> eventService.viewEventDetail(sc);
                case "4" -> eventService.listMyRegistrations(userId);
                case "5" -> eventService.registerEvent(sc, userId);
                case "6" -> eventService.cancelRegistration(sc, userId);
                case "0" -> {
                    System.out.println("이전 메뉴로 돌아갑니다.");
                    return;
                }
                default -> System.out.println("잘못된 선택입니다.");
            }
        }
    }
}
