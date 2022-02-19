package Homework6;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 1. Добавить на серверную сторону чата логирование, с выводом информации о действиях на сервере (запущен, произошла ошибка,
 * клиент подключился, клиент прислал сообщение/команду).
 * 2. Написать метод, которому в качестве аргумента передается не пустой одномерный целочисленный массив.
 * Метод должен вернуть новый массив, который получен путем вытаскивания из исходного массива элементов,
 * идущих после последней четверки. Входной массив должен содержать хотя бы одну четверку, иначе в методе необходимо выбросить
 * RuntimeException. Написать набор тестов для этого метода (по 3-4 варианта входных данных). Вх: [ 1 2 4 4 2 3 4 1 7 ] ->
 * вых: [ 1 7 ].
 * 3. Написать метод, который проверяет состав массива из чисел 1 и 4. Если в нем нет хоть одной четверки или единицы,
 * то метод вернет false; Написать набор тестов для этого метода (по 3-4 варианта входных данных).
 * 4. *Добавить на серверную сторону сетевого чата логирование событий.
 */
public class IntegerArray {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(ArrayAfter4(new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7})));
    }

    public static int[] ArrayAfter4(int[] array) {
        List<Integer> list = new ArrayList<>();
        for (int element : array) {
            list.add(element);
        }

        if ((!list.contains(4)) || (list.get(list.size()-1)==4)) throw new RuntimeException();

        int index = list.lastIndexOf(4);
        int[] newArray = new int[array.length - index - 1];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i + index + 1];
            if (i == newArray.length - 1) break;
        }
        return newArray;
    }


    public static boolean ArrayCheckNumbers(int[] array) {
        List<Integer> list = new ArrayList<>();
        for (int element : array) {
            list.add(element);
        }
        if (list.contains(1)&&list.contains(4)){
            return true;
        }
        else {
            return false;
        }
    }

}