# Power Meter - Smart Electricity Monitoring System

A sophisticated Android application for real-time electricity monitoring using ESP32 and PZEM-004T sensor. This system provides detailed power consumption analytics, cost calculations based on tiered pricing, and comprehensive usage visualization.

## Features

### Real-time Monitoring
- Voltage (V)
- Current (A)
- Power (W)
- Energy Consumption (kWh)
- Power Factor
- Frequency (Hz)

### Smart Analytics
- Daily usage patterns with line charts
- Monthly consumption with bar charts
- Peak demand tracking
- Power quality monitoring

### Cost Calculation
- Tiered pricing system (3 tiers)
- Customizable electricity rates
- Dynamic cost updates
- Demand charge calculation

### Device Management
- Multiple ESP32 device support
- Easy device addition and removal
- Location-based device organization
- Connection status monitoring

### User Interface
- Modern Material Design
- Intuitive dashboard layout
- Real-time data updates
- Responsive charts and graphs

## Technical Stack

### Android App
- Kotlin
- MVVM Architecture
- Material Design Components
- MPAndroidChart for data visualization
- Coroutines for async operations
- SharedPreferences for data persistence
- Gson for JSON handling

### Hardware
- ESP32 Microcontroller
- PZEM-004T v3.0 Power Sensor
- WiFi connectivity

## Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 or higher
- ESP32 Arduino IDE setup
- PZEM-004T v3.0 sensor

### Android App Setup
1. Clone the repository
   
2. Open in Android Studio
3. Sync Gradle files
4. Build and run the app

### ESP32 Setup
1. Install required libraries:
   - PZEM-004T
   - ArduinoJson
   - WiFi

2. Update WiFi credentials in ESP32_PZEM004T.ino
3. Upload the code to ESP32

## Hardware Connection

### PZEM-004T to ESP32
PZEM-004T | ESP32
TX | GPIO16 (RX2)
RX | GPIO17 (TX2)
5V | 5V
GND | GND
### Power Line Connections
Mains Live → PZEM-004T Live In
Mains Neutral → PZEM-004T Neutral In
Load Live → PZEM-004T Live Out
Load Neutral → PZEM-004T Neutral Out

## Usage

1. **Device Setup**
   - Add ESP32 device through app interface
   - Enter device name, IP address, and location
   - Connect to device

2. **Monitoring**
   - View real-time power readings
   - Track daily and monthly usage
   - Monitor power quality metrics

3. **Cost Management**
   - Configure electricity rates
   - Set tier thresholds
   - View cost calculations

## Safety Warnings

⚠️ **HIGH VOLTAGE - DANGER**
- This project involves mains voltage
- Installation should be done by qualified electricians
- Follow local electrical codes and regulations
- Use proper isolation and protection

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

- MPAndroidChart library
- Material Design Components
- PZEM-004T community
- ESP32 community

## Contact

Your Name - (vimukthixsandeepa)

Project Link: [https://github.com/yourusername/power-meter](https://github.com/yourusername/power-meter)

## Screenshots

[Add screenshots of your app here]



