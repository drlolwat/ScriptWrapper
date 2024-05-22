//package org.lolwat;
//
//import org.dreambot.api.Client;
//
//import static org.dreambot.api.utilities.Logger.log;
//
//public class Map implements Runnable {
//    @Override
//    public void run() {
//        while (true) {
//
//            logPos();
//
//            try {
//                Thread.sleep(5000); // Run map every 5 seconds
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//    }
//
//    private void logPos() {
//
//        StringBuilder jsonOutput = new StringBuilder();
//        jsonOutput.append("{");
//        jsonOutput.append("\"BB_X\": ").append(Client.getBase().getX()).append(", ");
//        jsonOutput.append("\"BB_Y\": ").append(Client.getBase().getY()).append(", ");
//        jsonOutput.append("\"BB_Z\": ").append(Client.getBase().getZ());
//        jsonOutput.append("}");
//
//        log("BB_XYZ: " + jsonOutput.toString());
//    }
//}