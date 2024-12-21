import java.util.concurrent.*;
import java.util.*;

//симуляция системы вызовов и лифтов
public class Simulation {
    private static final int  number_of_floors= 16; //кол-во этажей
    private static final int number_of_elevators = 2; //кол-во лифтов (потоки)

    private final List<Elevator> elevators = new ArrayList<>(); //список лифтов
    //пул потоков для параллельного выполнения задач лифтов и генерации вызовов
    //один дополнительно для обработки генерации вызовов лифта
    private final ExecutorService executor_service = Executors.newFixedThreadPool(number_of_elevators + 1);

    public static void main(String[] args) {
        new Simulation().run_simulation();
    }

    //возврат списка лифтов
    public List<Elevator> getElevators() {
        return elevators;
    }

    //запуск симуляции
    private void run_simulation() {
        //просим ввести пользователя продолжительность симуляции в секундах
        //до тех пор, пока не введёт правильно
        Scanner input = new Scanner(System.in);
        int simulation_duration = -1;
        while(simulation_duration <= 0){
            System.out.println("Введите продолжительность симуляции в секундах: ");
            if (input.hasNextInt()) {
                simulation_duration = input.nextInt();
                if (simulation_duration <= 0) {
                    System.out.println("Продолжительность должна быть натуральным числом.");
                }
            } else {
                System.out.println("Продолжительность должна быть натуральным числом.");
                input.next();
            }
            System.out.println("\nНАЧАЛО СИМУЛЯЦИИ");
            System.out.println("-".repeat(30));
        }
        //создание лифтов и добавление их в список
        for (int i = 0; i < number_of_elevators; i++) {
            elevators.add(new Elevator(i));
        }
        //добавление задачи генерации вызова в пул
        executor_service.submit(() -> generate_call());

        //добавление задачи запуска лифта в пул
        //для каждого лифта из списка
        for (Elevator elevator : elevators) {
            executor_service.submit(() -> elevator.run_elevator());
        }

        //ожидание окончания симуляции на время simulation_duration
        //после чего завершаем работу потоков
        try {
            Thread.sleep(simulation_duration * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor_service.shutdownNow();
        }
    }

    //генерация вызовов со случайным этажом и направлением
    private void generate_call() {
        Random value = new Random();
        //проверяем не прерывание
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int floor = value.nextInt(number_of_floors);
                Call call = new Call(floor);
                System.out.println("\nВызов лифта: " + call);
                //находим наиболее подходящий лифт для вызова
                Elevator bestElevator = findBestElevator(call);
                if (bestElevator != null && !bestElevator.is_duplicate(call)) {
                    //добавляем вызов в очередь к лифту
                    bestElevator.addCallToQueue(call);
                } else if (bestElevator != null){
                    System.out.println("К поступившему вызову уже направляется один из лифтов");
                }
                //вызовы генерятся каждую 1 секунду
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    //находим лучший лифт
    private Elevator findBestElevator(Call call) {
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator elevator : getElevators()) {
            int current_floor = elevator.getCurrentFloor();
            int target_floor = elevator.getCurrentTargetFloor();
            //если лифт уже находится на этаже из вызова, автоматически помечаем вызов обработанным этим лифтом
            if (current_floor == call.getFloor()){
                System.out.println("Лифт " + elevator.getNumber() + " обработал вызов: " + call.getFloor());
                break;
            }
            //проверка не находится ли вызов уже на пути лифта до какого-либо другого этажа
            boolean is_between_curr_and_targ = (current_floor < call.getFloor() && call.getFloor() < target_floor)
                    || (current_floor > call.getFloor() && call.getFloor() > target_floor);

            //если вызов находится по пути лифта, делаем лифт лучшим
            if (is_between_curr_and_targ){
                bestElevator = elevator;
                break;
            }
            //иначе вычисляем расстояние от текущего положения лифта до поступившего этажа
            // с учетом посещения целевого этажа (то есть который обрабатывается на данном этапе)
            // и выбираем вариант, где это расстояние меньше
            else{
                int distance = Math.abs(elevator.getCurrentTargetFloor() - call.getFloor()) +
                        Math.abs(elevator.getCurrentTargetFloor() - elevator.getCurrentFloor());
                if (distance < minDistance){
                    minDistance = distance;
                    bestElevator = elevator;
                }
            }
        }
        return bestElevator;
    }
}
