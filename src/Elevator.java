import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

//класс лифта
public class Elevator {
    private static final long time_for_floor = 500L; //время перемещения на один этаж - 0.5 секунд
    private final int number; //номер лифта
    private int current_floor = 0; //этаж, на котором в данный момент находится лифт
    private int current_target_floor = 0;
    //очередь для вызовов лифта
    private final PriorityBlockingQueue<Call> queue = new PriorityBlockingQueue<>(10,
            Comparator.comparingInt(call -> Math.abs(call.getFloor() - current_floor)));
    private final Set<Call> calls_in_process = new HashSet<>(); //структура для вызовов в процессе


    public Elevator(int number) {
        this.number = number;
    }

    public Elevator(){
        this.number = 0;
    }

    //возврат текущего этажа
    public int getCurrentFloor() {
        return current_floor;
    }

    //возврат номера лифта
    public int getNumber() {
        return number;
    }

    //возврат текущего целевого этажа
    public int getCurrentTargetFloor() {
        return current_target_floor;
    }

    //проверка не находится ли переданный вызов уже в обработке
    public boolean is_duplicate(Call call) {
        return calls_in_process.contains(call);
    }

    //добавление вызова в очередь к лифту
    public void addCallToQueue(Call call) {
        queue.add(call);
        calls_in_process.add(call);
        System.out.println("Лифт " + number + " берёт в обработку вызов: " + call);
    }

    //запуск лифта
    public void run_elevator() {
        try {
            // Проверяем на прерывание
            while (!Thread.currentThread().isInterrupted()) {
                Call nextCall = queue.poll(2, TimeUnit.SECONDS);
                if (nextCall != null) {
                    // Лифт перемещается на этаж
                    current_target_floor = nextCall.getFloor();
                    move(current_target_floor);
                    calls_in_process.remove(nextCall);
                } else {
                    // Иначе лифт стоит на месте
                    System.out.println("Лифт " + number + " ожидает вызов на этаже: " + current_floor);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    //перемещение лифта на этаж
    private void move(int target_floor) {
        int step = (target_floor > current_floor) ? 1 : -1; //направление движения

        //лифт двигается этаж за этаж, проверяя вызовы
        while (current_floor != target_floor) {
            try {
                Thread.sleep(time_for_floor); //время для перемещения на один этаж
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            //переход на следующий этаж
            current_floor += step;

            System.out.println("Лифт " + number + " на этаже " + current_floor);

            Call current_call = new Call(current_floor);
            //проверяем не является ли текущий этаж одним из тех, что должен обработать лифт
            //и если является, то убираем из очереди и из вызовов в процессе
            if (calls_in_process.contains(current_call)){
                System.out.println("Лифт " + number + " обработал вызов: " + current_call);
                calls_in_process.remove(current_call);
                queue.remove(current_call);
            }
        }
    }
}