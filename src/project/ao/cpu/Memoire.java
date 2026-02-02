package project.ao.cpu;
public class Memoire {
    private ROM rom;
    private RAM ram;
    public RAM getRam() { return ram; }
    public ROM getRom() { return rom; }

    public Memoire(byte[] programme) {
        this.rom = new ROM(programme);
        this.ram = new RAM();
    }

    public byte read(short adresse) {
        int adr = adresse & 0xFFFF;
        if (adr <= 0x03FF) {
            return ram.read(adresse);
        } else if (adr >= 0xFC00) {
            return rom.read(adresse);
        } else {
            return (byte) 0x00;     // zone vide : 0400–FBFF
        }
    }

    public void write(short adresse, byte valeur) {
        int adr = adresse & 0xFFFF;
        if (adr <= 0x03FF) {        // écriture uniquement en RAM
            ram.write(adresse, valeur);
        }
    }
}
