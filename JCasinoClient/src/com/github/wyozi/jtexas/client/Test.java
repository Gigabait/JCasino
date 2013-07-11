package com.github.wyozi.jtexas.client;

import java.io.File;

public class Test {

    /**
     * @param args
     */
    public static void main(final String[] args) {

        //renameCards();
        
        System.out.println(System.getProperty("os.name"));
        
        System.out.println(5 % 4);
        //new MainClient();
    }
    
    public static void renameCards() {
        final File folder = new File("assets/cards");
        final File newFolder = new File(folder, "back");
        if (!newFolder.exists()) {
            newFolder.mkdir();
        }
        for (final File f : folder.listFiles()) {
            
            final String name = f.getName();
            
            if (name.startsWith("b")) {
                f.renameTo(new File(newFolder, name));
            }
            int id;
            try {
                id = Integer.parseInt(f.getName().replace(".png", ""));
            }
            catch (final NumberFormatException e) {
                continue;
            }
            String newName = (id%4) + "_" + ((int)Math.floor((id-1)/4)) + ".png";
            if (id == 53 || id == 54) {
                newName = "4_" + (54-id) + ".png";
            }
            f.renameTo(new File(folder, newName));
        }
    }

}
