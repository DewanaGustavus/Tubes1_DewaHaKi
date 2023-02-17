# Tubes1_STIMA_GALAXIO_BOT_DewaHaKi

Tugas Besar I IF2211 Strategi Algoritma Semester II Tahun 2022/2023, Pemanfaatan Algoritma _Greedy_ dalam Aplikasi Permainan **Galaxio**

## Daftar Isi

- [Deskripsi Singkat Program dan Tugas](#deskripsi-singkat-program-dan-tugas)
- [Strategi Greedy Program](#strategi-greedy-program)
- [Struktur Program](#struktur-program)
- [Requirement Program](#requirement-program)
- [Cara Kompilasi dan Menjalankan Program](#cara-kompilasi-dan-menjalankan-program)
- [Link Demo Program](#link-demo-program)
- [Author Program](#author-program)

## Deskripsi Singkat Program dan Tugas

Galaxio adalah sebuah game battle royale yang mempertandingkan bot kapal pemain dengan beberapa bot kapal yang lain. Setiap pemain akan memiliki sebuah bot kapal dan tujuan dari permainan adalah agar bot kapal tetap hidup hingga akhir permainan. Pemain harus mengimplementasikan strategi tertentu pada bot untuk menentukan keputusan yang diambil bot pada saat jalannya permainan agar bot tersebut dapat memenangkan pertandingan.

Bahasa pemrograman yang digunakan untuk implementasi bot adalah Java dan dengan mengeimplementasikan algoritma _greedy_.

## Strategi Greedy Program

Dalam permainan **Galaxio** tujuan utamanya adalah menjadi pemain terakhir yang bertahan dalam arena atau mendapatkan poin terbesar untuk memenangkan game. Untuk mencapai tujuan tersebut, terdapat beberapa algoritma _greedy_ yang dapat diimplementasikan, sebagai berikut,

- Greedy by Distance
- Greedy by Virtual Point
- Greedy by Attacking Enemies
- Greedy by Avoiding
- Greedy by Eating

Penjelasan dari strategi _greedy_ di atas dapat dilihat pada file laporan yang terletak pada direktori _doc_ repositori ini.

## Struktur Program

```bash
.
├── Dockerfile
├── README.md
├── pom.xml
├── src
│   └── main
│       └── java
│           ├── Enums
│           │   ├── Effects.java
│           │   ├── ObjectTypes.java
│           │   └── PlayerActions.java
│           ├── META-INF
│           │   └── MANIFEST.MF
│           ├── Main.java
│           ├── Models
│           │   ├── GameObject.java
│           │   ├── GameState.java
│           │   ├── GameStateDto.java
│           │   ├── PlayerAction.java
│           │   ├── Position.java
│           │   └── World.java
│           ├── Services
│           │   ├── BotService.java
│           │   └── Strategy.java
│           ├── main.iml
│           ├── out
│           │   └── production
│           │       └── main
│           │           ├── META-INF
│           │           │   └── MANIFEST.MF
│           │           └── main.iml
│           └── script
│               ├── compile and run.bat
│               ├── compile.bat
│               ├── copyfolder.sh
│               ├── deleteLog.bat
│               ├── deleteLog.sh
│               ├── killterminal.bat
│               ├── run.bat
│               └── runx.bat
└── target
    ├── DewaHaKi.jar
    ├── classes
    ├── libs
    ├── maven-archiver
    └── maven-status
```

## Requirement Program 

- .Net Core 3.1
- .Net Core 5.x
- Java versi 11 (minimum)

## Cara Kompilasi dan Menjalankan Program

1. Download file `starter-pack.zip` pada link [berikut](https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2)
2. Unzip file `starter-pack.zip`
3. Clone repositori ini
4. Buka folder hasil unzip `starter-pack`
5. Lakukan konfigurasi jumlah bot yang ingin dimainkan pada file JSON ”appsettings.json” dalam folder “runner-publish” dan “engine-publish”
6. Buka terminal baru pada folder runner-publish.
7. Jalankan runner menggunakan perintah “dotnet GameRunner.dll”
8. Buka terminal baru pada folder engine-publish
9. Jalankan engine menggunakan perintah “dotnet Engine.dll”
10. Buka terminal baru pada folder logger-publish
11. Jalankan engine menggunakan perintah “dotnet Logger.dll”
12. Jalankan masing-masing bot sesuai dengan jumlah yang telah ditentukan pada langkah 5, dengan command `java -jar path_ke_file_jar_hasil_clone_repositori_ini` untuk menggunakan bot ini, atau command `dotnet path_to_GameRunner.dll_di_dalam_folder_starter_pack` untuk menjalankan reference bot.

> P.S. Langkah 6-12 dapat digantikan dengan membuat suatu script file `.bat` baru bagi yang menggunakan sistem operasi Windows atau mengedit script file `run.sh` yang telah disediakan bagi yang menggunakan sistem operasi berbasis unix. Kemdudian untuk menjalankan game hanya perlu menjalankan script file tersebut.

## Link Demo Program

## Author Program

Made with ❤️ by
- [Farizki Kurniawan (13521082)]()
- [I Putu Bakta Hari Sudewa (13521150)]()
- [Dewana Gustavus Haraka Otang (13521173)]()
