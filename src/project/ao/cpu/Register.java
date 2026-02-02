package project.ao.cpu;
public class Register {
    private byte flags;
    private int A; // Accumulator A (8 bits)
    private int B; // Accumulator B (8 bits)
    private int X; // Index X (16 bits)
    private int Y; // Index Y (16 bits)
    private int PC; // Program counter (16 bits)
    private int SP; // Stack pointer (16 bits)
    private int CC; // Condition Code Register
    private int s;   // Stack System
    private int u;   // Stack User
    private int dp;   // Direct Page

    // Masques pour CCR
    private static final int CC_E = 0x80;
    private static final int CC_F = 0x40;
    private static final int CC_H = 0x20;
    private static final int CC_I = 0x10;
    private static final int CC_N = 0x08;
    private static final int CC_Z = 0x04;
    private static final int CC_V = 0x02;
    private static final int CC_C = 0x01;

    public Register() {
        reset();
    }

    public int getA() { return A; }
    public void setA(int a) { A = a & 0xFF; }

    public int getB() { return B; }
    public void setB(int b) { B = b & 0xFF; }

    public int getX() { return X; }
    public void setX(int x) { X = x & 0xFFFF; }

    public int getY() { return Y; }
    public void setY(int y) { Y = y & 0xFFFF; }

    public int getPC() { return PC; }
    public void setPC(int pc) { PC = pc & 0xFFFF; }

    public int getSP() { return SP; }
    public void setSP(int sp) { SP = sp & 0xFFFF; }

    public int getS() { return s;}
    public void setS(int s) {this.s = s;}

    public int getU() { return u;}
    public void setU(int u) {this.u = u;}

    public int getDP() { return dp;}
    public void setDP(int dp) {this.dp = dp; }

    // GETTERS pour les flags
    public boolean isFlagC() { return (CC & CC_C) != 0; }
    public boolean isFlagZ() { return (CC & CC_Z) != 0; }
    public boolean isFlagN() { return (CC & CC_N) != 0; }
    public boolean isFlagV() { return (CC & CC_V) != 0; }
    public boolean isFlagH() { return (CC & CC_H) != 0; }
    public boolean isFlagI() { return (CC & CC_I) != 0; }
    public boolean isFlagF() { return (CC & CC_F) != 0; }
    public boolean isFlagE() { return (CC & CC_E) != 0; }

    public void setFlagC(boolean value) { if(value) CC |= CC_C; else CC &= ~CC_C; }
    public void setFlagZ(boolean value) { if(value) CC |= CC_Z; else CC &= ~CC_Z; }
    public void setFlagN(boolean value) { if(value) CC |= CC_N; else CC &= ~CC_N; }
    public void setFlagV(boolean value) { if(value) CC |= CC_V; else CC &= ~CC_V; }
    public void setFlagH(boolean value) { if(value) CC |= CC_H; else CC &= ~CC_H; }
    public void setFlagI(boolean value) { if(value) CC |= CC_I; else CC &= ~CC_I; }
    public void setFlagF(boolean value) { if(value) CC |= CC_F; else CC &= ~CC_F; }
    public void setFlagE(boolean value) { if(value) CC |= CC_E; else CC &= ~CC_E; }

    public void updateNZFlags8(int result) {
        setFlagN((result & 0x80) != 0);
        setFlagZ((result & 0xFF) == 0);
        setFlagV(false);
    }

    public void updateNZFlags16(int result) {
        setFlagN((result & 0x8000) != 0);
        setFlagZ((result & 0xFFFF) == 0);
        setFlagV(false);
    }

    public void updateHFlag(int a, int b) {
        setFlagH(((a & 0x0F) + (b & 0x0F)) > 0x0F);
    }
    public int getCC() {
        return CC & 0xFF;
    }


    public void reset() {
        A = 0; B = 0;
        X = 0; Y = 0;
        PC = 0xFC00; // PC start at ROM
        SP = 0xFFFF;
        CC = 0;
        s = 0;
        u = 0;
        dp = 0;
        setFlagI(true);
        setFlagF(true);
    }

    public void printState() {
        System.out.printf("A=%02X B=%02X X=%04X Y=%04X PC=%04X SP=%04X CC=%02X%n",
                A,B,X,Y,PC,SP,CC);
    }
    public byte getFlags(){
        return flags;
    }
    public void setFlags(byte flags){
        this.flags=flags;
    }
}
