package project.ao.cpu;

public class ROM {
    private byte[] memoire;

    public ROM(byte[] programme) {
        int tailleROM = 1024; // 0xFC00–0xFFFF inclus
        memoire = new byte[tailleROM];

        // Remplir par défaut avec 0xFF
        for (int i = 0; i < tailleROM; i++) {
            memoire[i] = (byte) 0xFF;
        }

        if (programme != null && programme.length > 0) {
            if (programme.length > tailleROM) {
                throw new IllegalArgumentException("ROM dépasse 1024 octets.");
            }
            for (int i = 0; i < programme.length; i++) {
                memoire[i] = programme[i];
            }
        }
    }

    public byte read(short adresse) {
        int adr = adresse & 0xFFFF;
        if (adr >= 0xFC00 && adr <= 0xFFFF) {
            int pos = adr - 0xFC00;
            return memoire[pos];
        }
        return (byte) 0xFF; // hors ROM → 0xFF
    }
}
