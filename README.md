# Mama Lele — Buku Kas Digital UKM

Aplikasi Android native sederhana untuk pencatatan keuangan UKM, menggunakan Kotlin dan SQLite lokal.

## Fitur
- Pencatatan uang masuk dan uang keluar
- Saldo dihitung otomatis
- Daftar transaksi dan penghapusan transaksi
- Laporan harian (tanggal saat ini)
- Laporan bulanan (bulan saat ini)
- Ekspor laporan ke PDF
- Data disimpan lokal di perangkat; fitur utama bekerja offline

## Cara membuat APK tanpa menjalankan Android Studio di komputer

Proyek ini sudah dilengkapi GitHub Actions untuk membangun APK di server cloud. Anda tetap perlu melakukan langkah satu kali untuk mengunggah proyek ke repositori GitHub melalui browser.

1. Masuk ke GitHub dan buat repositori baru bernama `MamaLeleAndroid`.
2. Ekstrak ZIP proyek ini di komputer.
3. Unggah seluruh isi folder `MamaLeleAndroid` ke repositori baru, termasuk folder tersembunyi `.github/workflows/build-apk.yml`.
4. Buka tab **Actions** pada repositori GitHub. Jika diminta, aktifkan Actions untuk repositori.
5. Pilih workflow **Build Mama Lele APK**, klik **Run workflow**, lalu klik tombol hijau **Run workflow**.
6. Tunggu sampai workflow selesai dengan tanda centang hijau.
7. Buka hasil workflow dan unduh artifact **MamaLele-debug-APK**. Ekstrak ZIP artifact untuk mendapatkan `app-debug.apk`.
8. Pindahkan APK ke HP Android, buka file tersebut, dan izinkan pemasangan aplikasi dari sumber itu bila Android meminta izin.

## Fitur workflow
Workflow cloud menggunakan Java 17, Gradle 8.9, dan Android SDK Platform 35 untuk menjalankan `assembleDebug`, kemudian mengunggah APK sebagai artifact.

## Catatan penting
- File APK belum dikompilasi atau diuji pada perangkat dalam lingkungan pembuatan proyek ini. Build cloud harus selesai tanpa error sebelum APK dapat diunduh.
- Ini adalah APK debug untuk uji coba, bukan rilis produksi yang ditandatangani untuk distribusi resmi.
- Sebelum dipakai untuk pencatatan usaha nyata, uji transaksi, saldo, laporan, dan ekspor PDF.
- PDF disimpan di direktori file khusus aplikasi.
