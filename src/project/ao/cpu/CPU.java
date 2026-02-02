package project.ao.cpu;

 class Cpu {

    private Register reg;
    private Memoire mem;
    private boolean halted = false;

    public Cpu(Memoire mem, Register reg) {
        this.mem = mem;
        this.reg = reg;
    }
    public Memoire getMem() {
        return mem;
    }
    public Register getRegister() {
        return reg;
    }

    public void reset() {
        halted = false;//JE dis au CPU "Tu n’es plus arrêté, tu peux recommencer à exécuter le programme"
        reg.reset(); // Reset tous les registres et flags
    }

    public boolean isHalted() { return halted; }

    private int fetch8() {//lire 1 octet
        //Ce qui se passe :
        int pc = reg.getPC();//Lire l’adresse courante du PC
        int value = mem.read((short) pc) & 0xFF;//Lire 1 octet depuis la mémoire
        reg.setPC(pc + 1);//Incrémenter le PC
        return value;//Retourner la valeur
    }

    private int fetch16() {//lire 2 octets(format big endian)
        int hi = fetch8();//lire l’octet de poids fort
        int lo = fetch8();//lire l’octet de poids faible
        return (hi << 8) | lo;
    }
    private void branchIf(boolean condition) {
        int offset = fetch8();
        if((offset & 0x80)!=0) offset |= 0xFFFFFF00;
        if(condition) reg.setPC(reg.getPC() + offset);
    }


    public void step() {//exécuter UNE instruction
        if(halted) return;//Si le CPU est arrêté → on sort

        int opcode = fetch8();//Le PC pointe maintenant sur l’opérande.

        switch(opcode) {//Décodage des instructions
            case 0x12: break; // NOP:Ne fait rien

            // LDA
            case 0x86: reg.setA(fetch8()); reg.updateNZFlags8(reg.getA()); break;// immediat   Met à jour N (Negative) et Z (Zero)
            case 0x96: reg.setA(mem.read((short) fetch8())); reg.updateNZFlags8(reg.getA()); break;// direct
            case 0xB6: reg.setA(mem.read((short) fetch16())); reg.updateNZFlags8(reg.getA()); break;// étendu

            // LDB
            case 0xC6: reg.setB(fetch8()); reg.updateNZFlags8(reg.getB()); break;
            case 0xD6: reg.setB(mem.read((short) fetch8())); reg.updateNZFlags8(reg.getB()); break;
            case 0xF6: reg.setB(mem.read((short) fetch16())); reg.updateNZFlags8(reg.getB()); break;
            // LDX
            case 0x8E: { int value = fetch16(); reg.setX(value);reg.updateNZFlags16(value); break;}
            case 0xBE: { int addr = fetch16(); int value = mem.read((short) addr); reg.setX(value); reg.updateNZFlags16(value); break; }
            // LDY
            case 0x10: {int op2 = fetch8(); switch (op2) {
                case 0x8E: {  int value = fetch16();reg.setY(value); reg.updateNZFlags16(value);break;}// immediat
                case 0xBE: {int addr = fetch16(); int value = mem.read((short) addr);reg.setY(value); reg.updateNZFlags16(value); break;}//étendu
                default://si op2 n’est ni 0x8E ni 0xBE, on tombe dans default
                    System.err.println("Unknown 0x10 opcode " + String.format("%02X", op2));
                    halted = true; //On arrête le CPU immédiatement
            }
                break;}
            // STA ::Écrit le contenu de A dans la mémoire
            case 0x97: mem.write((short) fetch8(), (byte) reg.getA()); break;// DIrect
            case 0xB7: mem.write((short) fetch16(), (byte) reg.getA()); break;//ETENDU
            case 0xA7: { // Indexé (exemple opcode A7)
                int offset = fetch8();
                if ((offset & 0x80) != 0) offset |= 0xFFFFFF00; // signe
                int addr = (reg.getX() + offset) & 0xFFFF;
                mem.write((short) addr, (byte) reg.getA());
                break;
            }
            // STB :Écrit le contenu de B dans la mémoire
            case 0xD7: { int addr = (reg.getDP() << 8) | fetch8(); mem.write((short) addr, (byte) reg.getB()); reg.updateNZFlags8(reg.getB()); break;}// DIrect
            case 0xF7: { int addr = fetch16(); mem.write((short) addr, (byte) reg.getB()); reg.updateNZFlags8(reg.getB());break;}//ETENDU
            case 0xE7: { // Indexé (exemple opcode E7)
                int offset = fetch8();
                if ((offset & 0x80) != 0) offset |= 0xFFFFFF00; // signe
                int addr = (reg.getX() + offset) & 0xFFFF;
                mem.write((short) addr, (byte) reg.getB());
                reg.updateNZFlags8(reg.getB());
                break;}
            // STX : stocke X dans mémoire
            case 0xDE: { // étendu
                int addr = fetch16();
                mem.write((short) addr, (byte) (reg.getX() >> 8));     // high byte
                mem.write((short) (addr + 1), (byte) (reg.getX() & 0xFF)); // low byte
                break;
            }
            // ADD A
            case 0x8B: addA(fetch8()); break;
            case 0x9B: addA(mem.read((short) fetch8())); break;
            case 0xBB: addA(mem.read((short) fetch16())); break;

            // ADD B
            case 0xCB: addB(fetch8()); break;
            case 0xDB: addB(mem.read((short) fetch8())); break;
            case 0xFB: addB(mem.read((short) fetch16())); break;
            // ADDX
            case 0x8F: { int value = fetch16();int x = reg.getX(); int res = (x + value) & 0xFFFF; reg.setX(res); reg.updateNZFlags16(res); reg.setFlagC(x + value > 0xFFFF);  break;}
            case 0xBF: {int addr = fetch16(); int value = mem.read((short) addr); int x = reg.getX(); int res = (x + value) & 0xFFFF; reg.setX(res);  reg.updateNZFlags16(res);  reg.setFlagC(x + value > 0xFFFF);break;}

            // SUB A :modifie le registre
            case 0x80: subA(fetch8()); break;
            case 0x90: subA(mem.read((short) fetch8())); break;
            case 0xB0: subA(mem.read((short) fetch16())); break;

            // SUB B :modifie le registre
            case 0xC0: subB(fetch8()); break;
            case 0xD0: subB(mem.read((short) fetch8())); break;
            case 0xF0: subB(mem.read((short) fetch16())); break;

            // CMP A:ne modifie rien, seulement les flags
            case 0x81: cmpA(fetch8()); break;
            case 0x91: cmpA(mem.read((short) fetch8())); break;
            case 0xB1: cmpA(mem.read((short) fetch16())); break;

            // CMP B:ne modifie rien, seulement les flags
            case 0xC1: cmpB(fetch8()); break;
            case 0xD1: cmpB(mem.read((short) fetch8())); break;
            case 0xF1: cmpB(mem.read((short) fetch16())); break;
            // INCA : incrémente A de 1
            case 0x4C: // opcode exemple pour INCA
                reg.setA((reg.getA() + 1) & 0xFF);
                reg.updateNZFlags8(reg.getA());
                break;

            // DECA : décrémente A de 1
            case 0x4A: // opcode exemple pour DECA
                reg.setA((reg.getA() - 1) & 0xFF);
                reg.updateNZFlags8(reg.getA());
                break;
            // CLRA : met A à 0
            case 0x4F:
                reg.setA(0);
                reg.updateNZFlags8(0);
                break;

            // CLRB : met B à 0
            case 0x5F:
                reg.setB(0);
                reg.updateNZFlags8(0);
                break;
            // ANDA
            case 0x84:
                int valA = fetch8(); // immédiat
                reg.setA(reg.getA() & valA);
                reg.updateNZFlags8(reg.getA());
                break;

            // ANDB
            case 0xC4:
                int valB = fetch8(); // immédiat
                reg.setB(reg.getB() & valB);
                reg.updateNZFlags8(reg.getB());
                break;
            // COMA : complément à 1
            case 0x43:
                reg.setA(~reg.getA() & 0xFF);
                reg.updateNZFlags8(reg.getA());
                reg.setFlagC(true); // Carry = 1 après COM
                break;

            // COMB : complément à 1
            case 0x53:
                reg.setB(~reg.getB() & 0xFF);
                reg.updateNZFlags8(reg.getB());
                reg.setFlagC(true);
                break;


            case 0x7E: reg.setPC(fetch16()); break;// JMP :fetch16 :lit l’adresse et reg.setPC(fetch16()) met le Program Counter à cette adresse
            case 0x3F: halted = true; break;// HALT (SWI):Arrêt du CPU (équivalent SWI ici)
            case 0x20: branchIf(true); break;       // BRA
            case 0x26: branchIf(!reg.isFlagZ()); break; // BNE
            case 0x27: branchIf(reg.isFlagZ()); break;  // BEQ
            case 0x2B: branchIf(reg.isFlagN()); break;  // BMI
            case 0x2A: branchIf(!reg.isFlagN()); break; // BPL


            default:
                System.err.println("Unknown opcode "+String.format("%02X",opcode)+" at PC="+String.format("%04X",reg.getPC()));
                halted = true;
        }
    }

    public void run() {
        while(!halted) step();
    }

    private void addA(int val) {
        int a = reg.getA();
        int res = (a+val)&0xFF;
        reg.setA(res);
        reg.updateNZFlags8(res);
        reg.updateHFlag(a,val);
        reg.setFlagC(a+val>0xFF);
    }

    private void addB(int val) {
        int b = reg.getB();
        int res = (b+val)&0xFF;
        reg.setB(res);
        reg.updateNZFlags8(res);
        reg.updateHFlag(b,val);
        reg.setFlagC(b+val>0xFF);
    }

    private void subA(int val) {
        int a = reg.getA();
        int res = (a-val)&0xFF;
        reg.setA(res);
        reg.updateNZFlags8(res);
        reg.setFlagC(a>=val);
    }

    private void subB(int val) {
        int b = reg.getB();
        int res = (b-val)&0xFF;
        reg.setB(res);
        reg.updateNZFlags8(res);
        reg.setFlagC(b>=val);
    }

    private void cmpA(int val) {
        int res = (reg.getA()-val)&0xFF;
        reg.updateNZFlags8(res);
        reg.setFlagC(reg.getA()>=val);
    }

    private void cmpB(int val) {
        int res = (reg.getB()-val)&0xFF;
        reg.updateNZFlags8(res);
        reg.setFlagC(reg.getB()>=val);
    }


}