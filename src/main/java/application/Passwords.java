package application;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

// Argon2id hashing + verification.
public final class Passwords {
    
    private static final int ITER = 3;  
    private static final int MEM_KB = 15 * 1024;
    private static final int PAR = 1;

    private static final Argon2 A2 = Argon2Factory.create(Argon2Types.ARGON2id);

    // Hash raw password
    public static String hash(String raw) {
        char[] pw = raw.toCharArray();
        try { return A2.hash(ITER, MEM_KB, PAR, pw); }
        finally { A2.wipeArray(pw); }
    }

    // Verify raw password against stored PHC string.
    public static boolean verify(String raw, String phc) {
        char[] pw = raw.toCharArray();
        try { return A2.verify(phc, pw); }
        finally { A2.wipeArray(pw); }
    }

    private Passwords() {}
}

