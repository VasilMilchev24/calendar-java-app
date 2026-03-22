package bg.tu_varna.sit;

/**
 * Входна точка на приложението.
 */
public class Main {

    /**
     * Създава инстанция на класа за входна точка.
     */
    public Main() {
    }

    /**
     * Стартира конзолното приложение за личен календар.
     *
     * @param args аргументи при стартиране
     */
    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.run();
    }
}
