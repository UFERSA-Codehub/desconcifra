public class DH {
    public static void main(String[] args) {
        executarTesteDH();
    }

    static void executarTesteDH() {
        long p, g, A, a, B, b;
        long chaveAlice, chaveBob;
        p = 17;
        System.out.println("Valor de p: " + p);
        g = 7;
        System.out.println("valor de g: " + g);
        a = 6;
        System.out.println("Número secreto de Alice: " + a);
        A = expMod(g, a, p);
        System.out.println("Valor de A em Alice: " + A);
        b = 3;
        System.out.println("Número secreto de Bob: " + b);
        B = expMod(g, b, p);
        System.out.println("Valor de B em Alice: " + B);
        chaveAlice = expMod(B, a, p);
        chaveBob = expMod(A, b, p);
        System.out.println("Chave secreta de Alice: " + chaveAlice);
        System.out.println("Chave secreta de Bob: " + chaveBob);
    }

    // Exponenciação modular: a elevado a b, módulo p
    static long expMod(long a, long b, long p) {
        long resultado = 1;
        for (int i = 0; i < b; i++) {
            resultado = (resultado * a) % p;
        }
        return resultado;
    }

}
