package project.ao.cpu;

public class RAM {
    private byte[] memoire = new byte[1024]; // 0x0000–0x03FF inclus

    public byte read(short adresse) {
        int pos = adresse & 0xFFFF;
        if (pos >= 0x0000 && pos <= 0x03FF) {
            return memoire[pos];
        }
        return (byte) 0; // hors RAM → 0
    }

    public void write(short adresse, byte valeur) {
        int pos = adresse & 0xFFFF;
        if (pos >= 0x0000 && pos <= 0x03FF) {
            memoire[pos] = valeur;
        }
    }
}
