//cmt
//IMPORTER TOUTES LES CLASSES DE PACKAGE (ARRAYLIST,LIST)
//Si opcode est non null, alors on applique .toUpperCase() pour mettre le //texte en majuscules.
//Si opcode est null, alors on met une chaîne vide

//Si operand est non null ==> on enlève les espaces autour avec .trim().
/*
 * //Si operand est null ==>on met "" //Detecte le mode d'adressage
 * //info:startsWith():méthode en Java,Elle permet de vérifier si une chaîne
 * //de caractères commence par une autre chaîne donnée. //Si l'operande
 * commence par $, c’est une adresse memoire en hexadecimal. //On enlève le $
 * avec substring(1) //Si l’operande contient une virgule, c’est un mode indexé.
 * //Méthode pour détecter les erreurs de syntaxe simple //info:equals():compare
 * 2 chaines (true si identiques) //info:contains():vérifie si une sous-chaine
 * est présente //info:isEmpty():vérifie si la chaine est vide*/
package project.ao.cpu;
import java.util.*;
public class Instruction {
    public final String opcode;
    public final String operand;
    public String mode;
    String code = "";

    public Instruction(String opcode, String operand) {
        this.opcode = opcode != null ? opcode.toUpperCase() : "";
        this.operand = operand != null ? operand.trim() : "";
    }

    public String detecteMode() {
        // END est une directive, pas une instruction
        if (opcode.equals("END")) {
            return "directive";
        }
        if (operand.isEmpty()) {
            return "inherent";
        } else if (operand.startsWith("#")) {
            return "immediat";
        } else if (operand.startsWith("$")) {
            String addr = operand.substring(1);
            if (addr.length() <= 2) {
                return "direct";
            } else {
                return "etendu";
            }
        } else if (operand.contains(",")) {
            if (operand.contains("[")) {
                return "indexe-indirect";
            }
            return "indexe";
        } else if (operand.startsWith("[")) {
            return "etendu-indirect";
        } else if (opcode.equals("BEQ") || opcode.equals("BNE") || opcode.equals("BMI") ||
                opcode.equals("BRA") || opcode.equals("BPL") ||
                opcode.equals("JMP") || opcode.equals("JSR")) {
            return "relatif";
        }
        return "Inconnu";
    }

    public String getOpcodeHex() {
        String mode = detecteMode();

        switch (opcode) {
            // CONTRÔLE
            case "NOP": code = "12"; break;
            case "SWI": code = "3F"; break;
            case "END": code = ""; break; // END n'a pas d'opcode

            // BRANCHES CONDITIONNELLES
            case "BEQ": code = "27"; break;
            case "BNE": code = "26"; break;
            case "BMI": code = "2B"; break;
            case "BRA": code = "20"; break;
            case "BPL": code = "2A"; break;

            // REGISTRES (Inherent)
            case "INCA": code = "4C"; break;
            case "DECA": code = "4A"; break;
            case "CLRA": code = "4F"; break;
            case "CLRB": code = "5F"; break;
            case "COMA": code = "43"; break;
            case "COMB": code = "53"; break;




            // LDA
            case "LDA":
                switch(mode) {
                    case "immediat": code = "86"; break;
                    case "direct": code = "96"; break;
                    case "etendu": code = "B6"; break;
                    case "indexe": code = "A6"; break;
                }
                break;

            // LDB
            case "LDB":
                switch(mode) {
                    case "immediat": code = "C6"; break;
                    case "direct": code = "D6"; break;
                    case "etendu": code = "F6"; break;
                    case "indexe": code = "E6"; break;
                }
                break;

            //  LDX
            case "LDX":
                switch(mode) {
                    case "immediat": code = "8E"; break;
                    case "direct": code = "9E"; break;
                    case "etendu": code = "BE"; break;
                    case "indexe": code = "AE"; break;
                }
                break;

            // LDY
            case "LDY":
                switch(mode) {
                    case "immediat": code = "10 8E"; break;
                    case "direct": code = "10 9E"; break;
                    case "etendu": code = "10 BE"; break;
                    case "indexe": code = "10 AE"; break;
                }
                break;

            // STX
            case "STX":
                switch(mode) {
                    case "direct": code = "9F"; break;
                    case "etendu": code = "BF"; break;
                    case "indexe": code = "AF"; break;
                }
                break;

            // STA
            case "STA":
                switch(mode) {
                    case "direct": code = "97"; break;
                    case "etendu": code = "B7"; break;
                    case "indexe": code = "A7"; break;
                }
                break;

            // STB
            case "STB":
                switch(mode) {
                    case "direct": code = "D7"; break;
                    case "etendu": code = "F7"; break;
                    case "indexe": code = "E7"; break;
                }
                break;

            // ADDA
            case "ADDA":
                switch(mode) {
                    case "immediat": code = "8B"; break;
                    case "direct": code = "9B"; break;
                    case "etendu": code = "BB"; break;
                    case "indexe": code = "AB"; break;
                }
                break;

            // ADDB
            case "ADDB":
                switch(mode) {
                    case "immediat": code = "CB"; break;
                    case "direct": code = "DB"; break;
                    case "etendu": code = "FB"; break;
                    case "indexe": code = "EB"; break;
                }
                break;

            //  SUBA
            case "SUBA":
                switch(mode) {
                    case "immediat": code = "80"; break;
                    case "direct": code = "90"; break;
                    case "etendu": code = "B0"; break;
                    case "indexe": code = "A0"; break;
                }
                break;

            // SUBB
            case "SUBB":
                switch(mode) {
                    case "immediat": code = "C0"; break;
                    case "direct": code = "D0"; break;
                    case "etendu": code = "F0"; break;
                    case "indexe": code = "E0"; break;
                }
                break;



            //  CMPA
            case "CMPA":
                switch(mode) {
                    case "immediat": code = "81"; break;
                    case "direct": code = "91"; break;
                    case "etendu": code = "B1"; break;
                    case "indexe": code = "A1"; break;
                }
                break;

            // CMPB
            case "CMPB":
                switch(mode) {
                    case "immediat": code = "C1"; break;
                    case "direct": code = "D1"; break;
                    case "etendu": code = "F1"; break;
                    case "indexe": code = "E1"; break;
                }
                break;

            // JMP
            case "JMP":
                switch(mode) {
                    case "direct": code = "0E"; break;
                    case "etendu": code = "7E"; break;
                    case "indexe": code = "6E"; break;
                }
                break;



            //  ANDA
            case "ANDA":
                switch(mode) {
                    case "immediat": code = "84"; break;
                    case "direct": code = "94"; break;
                    case "etendu": code = "B4"; break;
                    case "indexe": code = "A4"; break;
                }
                break;

            // ANDB
            case "ANDB":
                switch(mode) {
                    case "immediat": code = "C4"; break;
                    case "direct": code = "D4"; break;
                    case "etendu": code = "F4"; break;
                    case "indexe": code = "E4"; break;
                }
                break;
        }
        return code;
    }

    public boolean estSyntaxeValide() {
        String mode = detecteMode();

        if (mode.equals("Inconnu")) return false;
        if (opcode.isEmpty()) return false;

        switch (mode) {
            case "immediat":
                if (!operand.startsWith("#")) return false;
                String cleanOpcd = operand.substring(1).replace("$", "");
                if (cleanOpcd.isEmpty()) return false;
                if (cleanOpcd.length() != 2 && cleanOpcd.length() != 4) return false;
                if (!cleanOpcd.matches("[0-9A-Fa-f]+")) return false;
                break;

            case "direct":
                if (!operand.startsWith("$")) return false;
                cleanOpcd = operand.substring(1);
                if (cleanOpcd.length() > 2) return false;
                if (!cleanOpcd.matches("[0-9A-Fa-f]+")) return false;
                break;

            case "etendu":
                if (!operand.startsWith("$")) return false;
                cleanOpcd = operand.substring(1);
                if (cleanOpcd.length() != 2 && cleanOpcd.length() != 4) return false;
                if (!cleanOpcd.matches("[0-9A-Fa-f]+")) return false;
                break;

            case "indexe":
            case "indexe-indirect":
                if (!operand.contains(",")) return false;
                break;

            case "relatif":
                break;

            case "inherent":
                if (!operand.isEmpty()) return false;
                break;

            case "directive":
                // END ne nécessite pas de validation d'opérande
                break;

            default:
                return false;
        }

        return !getOpcodeHex().isEmpty() || opcode.equals("END");
    }

    public String getMessageErreur() {
        if (!estSyntaxeValide()) {
            return "Erreur : mauvaise syntaxe → '" + opcode + " " + operand + "'";
        }
        return "OK";
    }

    public byte[] assemble() {
        if (!estSyntaxeValide()) {
            return new byte[0];
        }

        // END ne génère aucun code machine
        if (opcode.equals("END")) {
            return new byte[0];
        }

        String opHex = getOpcodeHex().trim();
        List<Byte> octs = new ArrayList<>();

        // Ajouter l'opcode
        if (opHex.contains(" ")) {
            // Pour LDY (opcodes sur 2 octets)
            for (String part : opHex.split(" ")) {
                octs.add((byte) Integer.parseInt(part, 16));
            }
        } else {
            // Opcode simple (1 octet)
            octs.add((byte) Integer.parseInt(opHex, 16));
        }

        String cleanOpcd = operand.replaceAll("[#$,\\[\\]]", "").toUpperCase();
        String mode = detecteMode();

        switch (mode) {
            case "immediat":
                String immVal = operand.substring(1).replace("$", "");
                if (immVal.length() == 2) {
                    octs.add((byte) Integer.parseInt(immVal, 16));
                } else if (immVal.length() == 4) {
                    // Pour LDX #$1234, LDY #$5678 (16 bits) - BIG ENDIAN
                    octs.add((byte) Integer.parseInt(immVal.substring(0, 2), 16)); // high
                    octs.add((byte) Integer.parseInt(immVal.substring(2, 4), 16)); // low
                }
                break;

            case "direct":
                if (cleanOpcd.length() >= 1) {
                    if (cleanOpcd.length() == 1) {
                        octs.add((byte) Integer.parseInt("0" + cleanOpcd, 16));
                    } else {
                        octs.add((byte) Integer.parseInt(cleanOpcd.substring(0, 2), 16));
                    }
                }
                break;

            case "etendu":
                if (cleanOpcd.length() == 2) {
                    // Adresse courte $05 =>devient $0005
                    octs.add((byte) 0x00); // high = 00
                    octs.add((byte) Integer.parseInt(cleanOpcd, 16)); // bas
                } else if (cleanOpcd.length() == 4) {
                    // oct haut
                    octs.add((byte) Integer.parseInt(cleanOpcd.substring(0, 2), 16)); // haut
                    octs.add((byte) Integer.parseInt(cleanOpcd.substring(2, 4), 16)); // bas
                }
                break;

            case "indexe":
                if (operand.equals(",X")) {
                    octs.add((byte) 0x84);
                } else if (operand.equals(",Y")) {
                    octs.add((byte) 0xA4);
                } else if (operand.contains(",")) {
                    String offsetStr = operand.split(",")[0].replace("$", "");
                    if (!offsetStr.isEmpty()) {
                        try {
                            octs.add((byte) Integer.parseInt(offsetStr, 16));
                        } catch (Exception e) { }
                    }
                }
                break;

            case "relatif":
                // Pour BEQ, BNE, BMI, BRA, BPL
                octs.add((byte) 0x00);
                break;
        }
        byte[] result = new byte[octs.size()];
        for (int i = 0; i < octs.size(); i++) {
            result[i] = octs.get(i);
        }
        return result;
    }
}
