package com.emreacr.smartHome;

import java.io.*;
import java.util.*;

/**
 * Simple Smart Home Management Application
 */
interface Controllable {
    void turnOn();
    void turnOff();
    String getStatus();
}

abstract class Device implements Controllable {
    private String name;
    private int battery; // 0 - 100
    private boolean isOn;

    public Device(String name, int battery) {
        setName(name);
        setBattery(battery);
        this.isOn = false;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Device name cannot be empty.");
        }
        this.name = name.trim();
    }

    public int getBattery() {
        return battery;
    }

    public final void setBattery(int battery) {
        if (battery < 0 || battery > 100) {
            throw new IllegalArgumentException("Battery level must be between 0 and 100.");
        }
        this.battery = battery;
    }

    public boolean isOn() {
        return isOn;
    }

    protected void setOn(boolean on) {
        this.isOn = on;
    }

    protected void consumeBattery(int amount) {
        if (amount < 0) return;
        battery -= amount;
        if (battery < 0) battery = 0;
    }

    public void charge(int amount) {
        if (amount < 0) return;
        battery += amount;
        if (battery > 100) battery = 100;
    }

    @Override
    public void turnOn() {
        if (isOn) {
            System.out.println(name + " is already ON!");
        } else if (battery <= 0) {
            System.out.println(name + " cannot be turned ON. Battery is empty!");
        } else {
            isOn = true;
            System.out.println(name + " turned ON.");
        }
    }

    @Override
    public void turnOff() {
        if (!isOn) {
            System.out.println(name + " is already OFF!");
        } else {
            isOn = false;
            System.out.println(name + " turned OFF.");
        }
    }

    @Override
    public String toString() {
        return getStatus();
    }

    public abstract String getStatus();

    public abstract String getTypeId(); // for file save/load
}

class SmartLamp extends Device {
    private int brightness; // 0 - 100

    public SmartLamp(String name, int battery, int brightness) {
        super(name, battery);
        setBrightness(brightness);
    }

    public int getBrightness() {
        return brightness;
    }

    public final void setBrightness(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Brightness must be between 0 and 100.");
        }
        this.brightness = value;
    }

    @Override
    public String getStatus() {
        return "[Lamp] " + getName() +
                " - Battery: " + getBattery() + "%" +
                ", Status: " + (isOn() ? "ON" : "OFF") +
                ", Brightness: " + brightness;
    }

    @Override
    public String getTypeId() {
        return "LAMP";
    }
}

class RobotVacuum extends Device {
    private String mode; // eco, turbo, silent
    private static final Set<String> ALLOWED_MODES =
            new HashSet<>(Arrays.asList("eco", "turbo", "silent"));

    public RobotVacuum(String name, int battery, String mode) {
        super(name, battery);
        setMode(mode);
    }

    public String getMode() {
        return mode;
    }

    public final void setMode(String mode) {
        if (mode == null || mode.isBlank()) {
            throw new IllegalArgumentException("Mode cannot be empty.");
        }
        String lower = mode.toLowerCase();
        if (!ALLOWED_MODES.contains(lower)) {
            throw new IllegalArgumentException("Invalid mode. Allowed: eco, turbo, silent.");
        }
        this.mode = lower;
    }

    @Override
    public void turnOn() {
        if (getBattery() < 20) {
            System.out.println(getName() + " cannot start, battery too low (<20%).");
            return;
        }

        int drain;
        switch (mode) {
            case "turbo":
                drain = 15;
                break;
            case "eco":
                drain = 5;
                break;
            case "silent":
                drain = 3;
                break;
            default:
                drain = 5;
        }

        consumeBattery(drain);

        if (getBattery() <= 0) {
            System.out.println(getName() + " battery drained while starting! Shutting down.");
            setOn(false);
            return;
        }

        super.turnOn();
    }

    @Override
    public String getStatus() {
        return "[Robot Vacuum] " + getName() +
                " - Battery: " + getBattery() + "%" +
                ", Status: " + (isOn() ? "ON" : "OFF") +
                ", Mode: " + mode;
    }

    @Override
    public String getTypeId() {
        return "VACUUM";
    }
}

class LogManager {
    private final List<String> logs = new ArrayList<>();

    public void log(String message) {
        String time = new Date().toString();
        String entry = "[" + time + "] " + message;
        logs.add(entry);
        System.out.println("(LOG) " + entry);
    }

    public void printLogs() {
        System.out.println("\n===== LOGS =====");
        if (logs.isEmpty()) {
            System.out.println("No logs yet.");
            return;
        }
        for (String log : logs) {
            System.out.println(log);
        }
    }
}

class DeviceManager {
    private final List<Device> devices = new ArrayList<>();
    private final LogManager logManager;

    public DeviceManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void addDevice(Device device) {
        devices.add(device);
        logManager.log("Device added: " + device.getStatus());
    }

    public Device findByName(String name) {
        if (name == null) return null;
        String target = name.toLowerCase();
        for (Device d : devices) {
            if (d.getName().toLowerCase().equals(target)) {
                return d;
            }
        }
        return null;
    }

    public void turnAllOn() {
        for (Device d : devices) {
            d.turnOn();
            logManager.log("Turn ON requested for: " + d.getName());
        }
    }

    public void turnAllOff() {
        for (Device d : devices) {
            d.turnOff();
            logManager.log("Turn OFF requested for: " + d.getName());
        }
    }

    public void printStatuses() {
        System.out.println("\n===== DEVICE STATUS =====");
        if (devices.isEmpty()) {
            System.out.println("No devices added yet.");
            return;
        }
        for (Device d : devices) {
            System.out.println(d.getStatus());
        }
    }

    public void showLowBatteryDevices(int threshold) {
        System.out.println("\n===== LOW BATTERY DEVICES (<= " + threshold + "%) =====");
        boolean any = false;
        for (Device d : devices) {
            if (d.getBattery() <= threshold) {
                System.out.println(d.getStatus());
                any = true;
            }
        }
        if (!any) {
            System.out.println("No devices with low battery.");
        }
    }

    public void saveToFile(String fileName) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            for (Device d : devices) {
                if (d instanceof SmartLamp lamp) {
                    pw.println(lamp.getTypeId() + ";" +
                            lamp.getName() + ";" +
                            lamp.getBattery() + ";" +
                            lamp.isOn() + ";" +
                            lamp.getBrightness());
                } else if (d instanceof RobotVacuum vacuum) {
                    pw.println(vacuum.getTypeId() + ";" +
                            vacuum.getName() + ";" +
                            vacuum.getBattery() + ";" +
                            vacuum.isOn() + ";" +
                            vacuum.getMode());
                }
            }
            logManager.log("Devices saved to file: " + fileName);
            System.out.println("Devices successfully saved to " + fileName);
        } catch (IOException e) {
            System.out.println("Error while saving devices: " + e.getMessage());
        }
    }

    public void loadFromFile(String fileName) {
        devices.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // type;name;battery;isOn;extra
                String[] parts = line.split(";");
                if (parts.length < 5) continue;
                String type = parts[0];
                String name = parts[1];
                int battery = Integer.parseInt(parts[2]);
                boolean isOn = Boolean.parseBoolean(parts[3]);
                String extra = parts[4];

                Device device;
                if (type.equalsIgnoreCase("LAMP")) {
                    int brightness = Integer.parseInt(extra);
                    device = new SmartLamp(name, battery, brightness);
                } else if (type.equalsIgnoreCase("VACUUM")) {
                    device = new RobotVacuum(name, battery, extra);
                } else {
                    continue;
                }

                if (isOn) {
                    device.turnOn();
                }
                devices.add(device);
            }
            logManager.log("Devices loaded from file: " + fileName);
            System.out.println("Devices successfully loaded from " + fileName);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error while loading devices: " + e.getMessage());
        }
    }

    public void scheduleTurnOff(Device device, int delaySeconds) {
        if (device == null) return;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                device.turnOff();
                logManager.log("Scheduled turn OFF executed for: " + device.getName());
                timer.cancel();
            }
        }, delaySeconds * 1000L);
        logManager.log("Turn OFF scheduled for device: " + device.getName() +
                " after " + delaySeconds + " seconds.");
    }
}

public class smartHome {

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static String readNonEmptyLine(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine();
            if (!line.isBlank()) {
                return line.trim();
            }
            System.out.println("Input cannot be empty.");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LogManager logManager = new LogManager();
        DeviceManager deviceManager = new DeviceManager(logManager);

        boolean running = true;

        while (running) {
            System.out.println("\n===== SMART HOME MENU =====");
            System.out.println("1 - Add new device");
            System.out.println("2 - Turn ALL devices ON");
            System.out.println("3 - Turn ALL devices OFF");
            System.out.println("4 - Show all devices status");
            System.out.println("5 - Search device by name");
            System.out.println("6 - Change lamp brightness");
            System.out.println("7 - Change robot vacuum mode");
            System.out.println("8 - Show low-battery devices");
            System.out.println("9 - Save devices to file");
            System.out.println("10 - Load devices from file");
            System.out.println("11 - Schedule device turn OFF");
            System.out.println("12 - Show logs");
            System.out.println("0 - Exit");

            int choice = readInt(sc, "Your choice: ");

            switch (choice) {
                case 1 -> {
                    System.out.println("\nDevice type: 1-Lamp, 2-Robot Vacuum");
                    int type = readInt(sc, "Type: ");
                    try {
                        String name = readNonEmptyLine(sc, "Device name: ");
                        int battery = readInt(sc, "Battery level (0-100): ");

                        if (type == 1) {
                            int brightness = readInt(sc, "Brightness (0-100): ");
                            Device lamp = new SmartLamp(name, battery, brightness);
                            deviceManager.addDevice(lamp);
                        } else if (type == 2) {
                            String mode = readNonEmptyLine(sc, "Mode (eco/turbo/silent): ");
                            Device vacuum = new RobotVacuum(name, battery, mode);
                            deviceManager.addDevice(vacuum);
                        } else {
                            System.out.println("Unknown device type.");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error while creating device: " + e.getMessage());
                    }
                }
                case 2 -> deviceManager.turnAllOn();
                case 3 -> deviceManager.turnAllOff();
                case 4 -> deviceManager.printStatuses();
                case 5 -> {
                    String searchName = readNonEmptyLine(sc, "Enter device name to search: ");
                    Device found = deviceManager.findByName(searchName);
                    if (found != null) {
                        System.out.println("Device found: " + found.getStatus());
                    } else {
                        System.out.println("No device found with this name.");
                    }
                }
                case 6 -> {
                    String name = readNonEmptyLine(sc, "Lamp name: ");
                    Device d = deviceManager.findByName(name);
                    if (d instanceof SmartLamp lamp) {
                        int newBrightness = readInt(sc, "New brightness (0-100): ");
                        try {
                            lamp.setBrightness(newBrightness);
                            System.out.println("Brightness updated.");
                            logManager.log("Brightness updated for " + lamp.getName() +
                                    " to " + newBrightness);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Lamp not found or device is not a lamp.");
                    }
                }
                case 7 -> {
                    String name = readNonEmptyLine(sc, "Robot vacuum name: ");
                    Device d = deviceManager.findByName(name);
                    if (d instanceof RobotVacuum vacuum) {
                        String newMode = readNonEmptyLine(sc, "New mode (eco/turbo/silent): ");
                        try {
                            vacuum.setMode(newMode);
                            System.out.println("Mode updated.");
                            logManager.log("Mode updated for " + vacuum.getName() +
                                    " to " + vacuum.getMode());
                        } catch (IllegalArgumentException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Robot vacuum not found or device is not a vacuum.");
                    }
                }
                case 8 -> {
                    int threshold = readInt(sc, "Battery threshold (e.g. 20): ");
                    deviceManager.showLowBatteryDevices(threshold);
                }
                case 9 -> {
                    String fileName = readNonEmptyLine(sc, "File name to save (e.g. devices.txt): ");
                    deviceManager.saveToFile(fileName);
                }
                case 10 -> {
                    String fileName = readNonEmptyLine(sc, "File name to load: ");
                    deviceManager.loadFromFile(fileName);
                }
                case 11 -> {
                    String name = readNonEmptyLine(sc, "Device name to schedule OFF: ");
                    Device d = deviceManager.findByName(name);
                    if (d == null) {
                        System.out.println("Device not found.");
                    } else {
                        int seconds = readInt(sc, "Turn OFF after how many seconds? ");
                        deviceManager.scheduleTurnOff(d, seconds);
                    }
                }
                case 12 -> logManager.printLogs();
                case 0 -> {
                    System.out.println("Exiting Smart Home. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice, please try again.");
            }
        }

        sc.close();
    }
}
