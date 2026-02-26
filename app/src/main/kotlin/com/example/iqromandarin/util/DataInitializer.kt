package com.example.iqromandarin.util

import android.content.Context
import com.example.iqromandarin.database.AppDatabase
import com.example.iqromandarin.model.Halaman
import com.example.iqromandarin.model.Item
import com.example.iqromandarin.model.Jilid
import com.example.iqromandarin.model.Progres
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Populates the database on first run from assets/json/data.json.
 * 12 Jilid, each with halaman and items.
 */
object DataInitializer {

    data class RawItem(
        val pinyin: String = "",
        val hanzi: String? = null,
        val indo_like: String = "",
        val arti: String = "",
        val contoh: String? = null,
        val initial: String? = null,
        val final: String? = null,
        val kalimat: String? = null,
        val cerita_id: Int? = null,
        val judul: String? = null,
        val isi: String? = null
    )

    data class DataJson(
        val initials: List<RawItem> = emptyList(),
        val finals: List<RawItem> = emptyList(),
        val hanzi: List<RawItem> = emptyList(),
        val kalimat: List<RawItem> = emptyList(),
        val cerita: List<RawItem> = emptyList()
    )

    suspend fun populateDatabase(context: Context, db: AppDatabase) {
        // Read JSON
        val json = context.assets.open("json/data.json").bufferedReader().use { it.readText() }
        val data = Gson().fromJson(json, DataJson::class.java)

        // --- Setup 12 Jilid ---
        val jilidList = listOf(
            Jilid(id=1, nomorJilid=1, nama="Konsonan Awal (Initials)", namaCina="声母", deskripsi="21 konsonan dasar Mandarin dengan perkiraan bunyi Indonesia", warna="#E53935", isUnlocked=true, totalHalaman=5, icon="🔤"),
            Jilid(id=2, nomorJilid=2, nama="Vokal Tunggal (Finals)", namaCina="韵母", deskripsi="6 vokal dasar dan penggabungan suku kata", warna="#43A047", isUnlocked=false, totalHalaman=4, icon="🗣️"),
            Jilid(id=3, nomorJilid=3, nama="Vokal Gabungan & Nasal", namaCina="复韵母", deskripsi="13 compound finals + 16 nasal finals", warna="#1E88E5", isUnlocked=false, totalHalaman=6, icon="📢"),
            Jilid(id=4, nomorJilid=4, nama="Hanzi Pictografis", namaCina="象形字", deskripsi="50 hanzi paling mudah yang berasal dari gambar", warna="#F4511E", isUnlocked=false, totalHalaman=10, icon="🖼️"),
            Jilid(id=5, nomorJilid=5, nama="Hanzi Umum (51-100)", namaCina="常用字", deskripsi="Top 50 hanzi paling sering dipakai", warna="#8E24AA", isUnlocked=false, totalHalaman=10, icon="✍️"),
            Jilid(id=6, nomorJilid=6, nama="Kata Gabungan", namaCina="词语", deskripsi="Kata-kata dari 2-3 hanzi yang sering dipakai", warna="#00ACC1", isUnlocked=false, totalHalaman=6, icon="🔗"),
            Jilid(id=7, nomorJilid=7, nama="Kalimat Sehari-hari", namaCina="日常用语", deskripsi="20 kalimat esensial untuk percakapan dasar", warna="#FFB300", isUnlocked=false, totalHalaman=4, icon="💬"),
            Jilid(id=8, nomorJilid=8, nama="Cerita Pendek (Graded)", namaCina="短篇故事", deskripsi="5 cerita pendek dengan kosakata terkontrol", warna="#6D4C41", isUnlocked=false, totalHalaman=5, icon="📖"),
            Jilid(id=9, nomorJilid=9, nama="SRS Review Dasar", namaCina="复习基础", deskripsi="Ulang item yang salah dengan sistem Leitner", warna="#546E7A", isUnlocked=false, totalHalaman=3, icon="🔄"),
            Jilid(id=10, nomorJilid=10, nama="SRS Review Lanjutan", namaCina="高级复习", deskripsi="Interval panjang, campuran semua jilid sebelumnya", warna="#37474F", isUnlocked=false, totalHalaman=3, icon="⭐"),
            Jilid(id=11, nomorJilid=11, nama="Immersion Mendengarkan", namaCina="听力训练", deskripsi="Dialog audio sederhana, latihan shadowing", warna="#00695C", isUnlocked=false, totalHalaman=4, icon="🎧"),
            Jilid(id=12, nomorJilid=12, nama="Immersion Mandiri", namaCina="自主学习", deskripsi="Import kalimat sendiri, tambah vocab personal", warna="#283593", isUnlocked=false, totalHalaman=2, icon="🌟")
        )
        db.jilidDao().insertAllJilid(jilidList)

        // --- Init Progres ---
        db.progresDao().insertProgres(Progres(id = 1, jilidAktif = 1, halamanAktif = 1))

        // --- Populate Jilid 1: Initials (21 Konsonan) ---
        populateJilid1(db, data.initials)

        // --- Populate Jilid 2: Finals ---
        populateJilid2(db, data.finals)

        // --- Populate Jilid 3: Compound & Nasal Finals ---
        populateJilid3(db)

        // --- Populate Jilid 4: Pictographic Hanzi ---
        populateJilid4(db, data.hanzi.take(50))

        // --- Populate Jilid 5: Common Hanzi 51-100 ---
        populateJilid5(db, data.hanzi.drop(50).take(50))

        // --- Populate Jilid 6: Compound Words ---
        populateJilid6(db)

        // --- Populate Jilid 7: Daily Sentences ---
        populateJilid7(db, data.kalimat)

        // --- Populate Jilid 8: Stories ---
        populateJilid8(db, data.cerita)

        // --- Populate Jilid 9-12: SRS & Immersion placeholders ---
        populateJilid9to12(db)
    }

    private suspend fun populateJilid1(db: AppDatabase, initials: List<RawItem>) {
        // 21 initials split into 5 halaman (4-5 per page)
        val halamanList = listOf(
            Halaman(id=1, jilidId=1, nomorHalaman=1, judul="b, p, m, f", isUnlocked=true),
            Halaman(id=2, jilidId=1, nomorHalaman=2, judul="d, t, n, l"),
            Halaman(id=3, jilidId=1, nomorHalaman=3, judul="g, k, h"),
            Halaman(id=4, jilidId=1, nomorHalaman=4, judul="j, q, x"),
            Halaman(id=5, jilidId=1, nomorHalaman=5, judul="zh, ch, sh, r, z, c, s, y, w")
        )
        db.halamanDao().insertAllHalaman(halamanList)

        val halamanMap = listOf(1,1,1,1,  2,2,2,2,  3,3,3,  4,4,4,  5,5,5,5,5,5,5)
        initials.take(21).forEachIndexed { index, raw ->
            val displayPinyin = raw.initial ?: raw.pinyin
            val contohStr = raw.contoh ?: ""
            db.itemDao().insertItem(Item(
                pinyin = displayPinyin,
                hanzi = null,
                indoPron = raw.indo_like,
                arti = "Konsonan: $displayPinyin",
                contoh = contohStr,
                kategori = "INITIAL",
                halamanId = halamanMap.getOrElse(index) { 5 },
                jilidId = 1
            ))
        }
    }

    private suspend fun populateJilid2(db: AppDatabase, finals: List<RawItem>) {
        val halamanList = listOf(
            Halaman(id=6, jilidId=2, nomorHalaman=1, judul="a, o, e, i, u, ü"),
            Halaman(id=7, jilidId=2, nomorHalaman=2, judul="Suku kata: ba, bo, bu, bi..."),
            Halaman(id=8, jilidId=2, nomorHalaman=3, judul="Suku kata: ma, me, mi, mo, mu..."),
            Halaman(id=9, jilidId=2, nomorHalaman=4, judul="Latihan Nada 1-4")
        )
        db.halamanDao().insertAllHalaman(halamanList)

        val halamanMap = listOf(6,6,6,6,6,6, 7,7,7,7, 8,8,8,8)
        finals.take(14).forEachIndexed { index, raw ->
            val displayPinyin = raw.final ?: raw.pinyin
            db.itemDao().insertItem(Item(
                pinyin = displayPinyin,
                hanzi = null,
                indoPron = raw.indo_like,
                arti = "Vokal: $displayPinyin",
                contoh = raw.contoh,
                kategori = "FINAL",
                halamanId = halamanMap.getOrElse(index) { 7 },
                jilidId = 2
            ))
        }

        // Tone practice items in halaman 4
        val tonePractice = listOf(
            Item(pinyin="mā", indoPron="ma (datar tinggi)", arti="Ibu 妈", contoh="妈妈 māma = Ibu", kategori="NADA", halamanId=9, jilidId=2),
            Item(pinyin="má", indoPron="ma (naik, seperti tanya?)", arti="Rami 麻", contoh="麻烦 máfan = Repot", kategori="NADA", halamanId=9, jilidId=2),
            Item(pinyin="mǎ", indoPron="ma (turun-naik, seperti 'maa~h')", arti="Kuda 马", contoh="马上 mǎshang = Segera", kategori="NADA", halamanId=9, jilidId=2),
            Item(pinyin="mà", indoPron="ma (turun cepat, tegas!)", arti="Memaki 骂", contoh="骂人 mà rén = Memaki", kategori="NADA", halamanId=9, jilidId=2),
            Item(pinyin="ma", indoPron="ma (tanpa tekanan, pendek)", arti="Partikel tanya 吗", contoh="你好吗? Nǐ hǎo ma? = Apa kabar?", kategori="NADA", halamanId=9, jilidId=2)
        )
        tonePractice.forEach { db.itemDao().insertItem(it) }
    }

    private suspend fun populateJilid3(db: AppDatabase) {
        val halamanList = (10..15).map { id ->
            Halaman(id=id, jilidId=3, nomorHalaman=id-9,
                judul=when(id) {
                    10 -> "ai, ei, ao, ou"
                    11 -> "ia, ie, ua, uo, üe"
                    12 -> "an, en, in, un, ün"
                    13 -> "ang, eng, ing, ong"
                    14 -> "ian, uan, üan"
                    15 -> "iang, uang, iong"
                    else -> "Final gabungan"
                })
        }
        db.halamanDao().insertAllHalaman(halamanList)

        val compoundItems = listOf(
            // ai, ei, ao, ou
            Item(pinyin="āi", indoPron="a-i (cepat, seperti 'hai')", arti="Ekspresi: Aduh!", contoh="哎 āi = Aduh!", kategori="FINAL_GABUNGAN", halamanId=10, jilidId=3),
            Item(pinyin="ài", indoPron="ai (seperti 'hai' indo)", arti="Cinta 爱", contoh="爱 ài = Cinta", kategori="FINAL_GABUNGAN", halamanId=10, jilidId=3),
            Item(pinyin="ēi", indoPron="ei (seperti 'hey')", arti="Panggilan: Hei!", contoh="诶 ēi = Hei!", kategori="FINAL_GABUNGAN", halamanId=10, jilidId=3),
            Item(pinyin="fēi", indoPron="fei (f + ei, seperti 'fei')", arti="Terbang 飞", contoh="飞机 fēijī = Pesawat terbang", kategori="FINAL_GABUNGAN", halamanId=10, jilidId=3),
            Item(pinyin="āo", indoPron="a-o (a lalu o, cepat)", arti="Ow! (ekspresi)", contoh="哦 ó = Oh!", kategori="FINAL_GABUNGAN", halamanId=10, jilidId=3),
            Item(pinyin="hǎo", indoPron="hao (h + a-o, seperti 'hao')", arti="Baik 好", contoh="你好 nǐhǎo = Halo", kategori="FINAL_GABUNGAN", halamanId=10, jilidId=3),
            Item(pinyin="ōu", indoPron="o-u (o lalu u, seperti 'ou')", arti="Oh! (ekspresi)", contoh="欧 ōu = Eropa", kategori="FINAL_GABUNGAN", halamanId=10, jilidId=3),
            Item(pinyin="zǒu", indoPron="zou (dz + o-u)", arti="Berjalan 走", contoh="走路 zǒulù = Berjalan kaki", kategori="FINAL_GABUNGAN", halamanId=10, jilidId=3),
            // Nasal
            Item(pinyin="ān", indoPron="an (a + n, seperti 'an' di 'aman')", arti="Aman 安", contoh="安全 ānquán = Aman", kategori="FINAL_NASAL", halamanId=12, jilidId=3),
            Item(pinyin="rén", indoPron="ren (r + e + n, 'ren')", arti="Orang 人", contoh="中国人 Zhōngguórén = Orang China", kategori="FINAL_NASAL", halamanId=12, jilidId=3),
            Item(pinyin="mén", indoPron="men (m + e + n)", arti="Pintu 门", contoh="大门 dàmén = Gerbang", kategori="FINAL_NASAL", halamanId=12, jilidId=3),
            Item(pinyin="shāng", indoPron="shang (sy + a + ng)", arti="Di atas 上", contoh="上面 shàngmiàn = Di atas", kategori="FINAL_NASAL", halamanId=13, jilidId=3),
            Item(pinyin="péng", indoPron="peng (p + e + ng)", arti="Teman (dlm 朋友)", contoh="朋友 péngyǒu = Teman", kategori="FINAL_NASAL", halamanId=13, jilidId=3),
            Item(pinyin="míng", indoPron="ming (m + i + ng)", arti="Cerah/Nama 明", contoh="明天 míngtiān = Besok", kategori="FINAL_NASAL", halamanId=13, jilidId=3)
        )
        compoundItems.forEach { db.itemDao().insertItem(it) }
    }

    private suspend fun populateJilid4(db: AppDatabase, hanziItems: List<RawItem>) {
        // 10 halaman, 5 hanzi per halaman
        val halamanList = (16..25).mapIndexed { idx, id ->
            Halaman(id=id, jilidId=4, nomorHalaman=idx+1,
                judul="Hanzi Pictografis ${idx*5+1}-${(idx+1)*5}")
        }
        db.halamanDao().insertAllHalaman(halamanList)

        hanziItems.forEachIndexed { index, raw ->
            val halamanIndex = index / 5
            val halamanId = 16 + halamanIndex
            db.itemDao().insertItem(Item(
                pinyin = raw.pinyin,
                hanzi = raw.hanzi,
                indoPron = raw.indo_like,
                arti = raw.arti,
                contoh = raw.contoh,
                kategori = "HANZI",
                halamanId = halamanId,
                jilidId = 4
            ))
        }
    }

    private suspend fun populateJilid5(db: AppDatabase, hanziItems: List<RawItem>) {
        val halamanList = (26..35).mapIndexed { idx, id ->
            Halaman(id=id, jilidId=5, nomorHalaman=idx+1,
                judul="Hanzi Umum ${idx*5+51}-${(idx+1)*5+51}")
        }
        db.halamanDao().insertAllHalaman(halamanList)

        hanziItems.forEachIndexed { index, raw ->
            val halamanId = 26 + (index / 5)
            db.itemDao().insertItem(Item(
                pinyin = raw.pinyin,
                hanzi = raw.hanzi,
                indoPron = raw.indo_like,
                arti = raw.arti,
                contoh = raw.contoh,
                kategori = "HANZI",
                halamanId = halamanId,
                jilidId = 5
            ))
        }
    }

    private suspend fun populateJilid6(db: AppDatabase) {
        val halamanList = (36..41).mapIndexed { idx, id ->
            Halaman(id=id, jilidId=6, nomorHalaman=idx+1,
                judul=when(idx) {
                    0 -> "Kata Sapaan & Orang"
                    1 -> "Kata Tempat & Waktu"
                    2 -> "Kata Kerja Umum"
                    3 -> "Kata Sifat Dasar"
                    4 -> "Angka & Ukuran"
                    else -> "Kata Campuran"
                })
        }
        db.halamanDao().insertAllHalaman(halamanList)

        val wordItems = listOf(
            Item(pinyin="nǐ hǎo", hanzi="你好", indoPron="ni hao (ni=ni, hao=hao)", arti="Halo / Selamat datang", contoh="你好！= Halo!", kategori="KATA", halamanId=36, jilidId=6),
            Item(pinyin="xièxie", hanzi="谢谢", indoPron="sie sie (s+ie, ie spt sia-sia)", arti="Terima kasih", contoh="谢谢你！= Terima kasih!", kategori="KATA", halamanId=36, jilidId=6),
            Item(pinyin="duìbuqǐ", hanzi="对不起", indoPron="dwei bu chi", arti="Maaf / Minta maaf", contoh="对不起！= Maaf!", kategori="KATA", halamanId=36, jilidId=6),
            Item(pinyin="méiguānxi", hanzi="没关系", indoPron="mei gwan si", arti="Tidak apa-apa", contoh="没关系！= Tidak apa-apa!", kategori="KATA", halamanId=36, jilidId=6),
            Item(pinyin="zàijiàn", hanzi="再见", indoPron="dze cien", arti="Sampai jumpa", contoh="再见！= Selamat tinggal!", kategori="KATA", halamanId=36, jilidId=6),
            Item(pinyin="Zhōngguó", hanzi="中国", indoPron="Jong gwo", arti="China / Tiongkok", contoh="中国人 = Orang China", kategori="KATA", halamanId=37, jilidId=6),
            Item(pinyin="xuéxiào", hanzi="学校", indoPron="syue siao", arti="Sekolah", contoh="我去学校。= Saya pergi ke sekolah.", kategori="KATA", halamanId=37, jilidId=6),
            Item(pinyin="yīyuàn", hanzi="医院", indoPron="i yuan", arti="Rumah sakit", contoh="医院在哪里？= RS di mana?", kategori="KATA", halamanId=37, jilidId=6),
            Item(pinyin="míngtiān", hanzi="明天", indoPron="ming tien", arti="Besok", contoh="明天见！= Sampai besok!", kategori="KATA", halamanId=37, jilidId=6),
            Item(pinyin="jīn tiān", hanzi="今天", indoPron="jin tien", arti="Hari ini", contoh="今天天气好。= Cuaca hari ini bagus.", kategori="KATA", halamanId=37, jilidId=6),
            Item(pinyin="chī fàn", hanzi="吃饭", indoPron="chr fan", arti="Makan (nasi/makanan)", contoh="吃饭了吗？= Sudah makan?", kategori="KERJA", halamanId=38, jilidId=6),
            Item(pinyin="hē shuǐ", hanzi="喝水", indoPron="he shwei", arti="Minum air", contoh="我要喝水。= Saya mau minum air.", kategori="KERJA", halamanId=38, jilidId=6),
            Item(pinyin="shuìjiào", hanzi="睡觉", indoPron="shwey ciao", arti="Tidur", contoh="我要睡觉了。= Saya mau tidur.", kategori="KERJA", halamanId=38, jilidId=6),
            Item(pinyin="xuéxí", hanzi="学习", indoPron="syue si", arti="Belajar", contoh="我在学习中文。= Saya sedang belajar Mandarin.", kategori="KERJA", halamanId=38, jilidId=6),
            Item(pinyin="zuò gōngkè", hanzi="做功课", indoPron="dzwo gong ke", arti="Mengerjakan PR", contoh="我做功课。= Saya mengerjakan PR.", kategori="KERJA", halamanId=38, jilidId=6),
            Item(pinyin="hǎo", hanzi="好", indoPron="hao (h+ao)", arti="Baik / Bagus", contoh="非常好！= Sangat bagus!", kategori="SIFAT", halamanId=39, jilidId=6),
            Item(pinyin="dà", hanzi="大", indoPron="da (d+a, tegas)", arti="Besar", contoh="大学 = Universitas", kategori="SIFAT", halamanId=39, jilidId=6),
            Item(pinyin="xiǎo", hanzi="小", indoPron="siao (s+iao)", arti="Kecil", contoh="小孩 = Anak kecil", kategori="SIFAT", halamanId=39, jilidId=6),
            Item(pinyin="hǎo chī", hanzi="好吃", indoPron="hao chr", arti="Enak / Lezat", contoh="这个很好吃！= Ini sangat enak!", kategori="SIFAT", halamanId=39, jilidId=6),
            Item(pinyin="piàoliang", hanzi="漂亮", indoPron="piao liang", arti="Cantik / Indah", contoh="你很漂亮！= Kamu sangat cantik!", kategori="SIFAT", halamanId=39, jilidId=6),
            Item(pinyin="yī ér sān sì wǔ", hanzi="一二三四五", indoPron="i er san si wu", arti="Satu dua tiga empat lima (1-5)", contoh="一二三！= Satu, dua, tiga!", kategori="ANGKA", halamanId=40, jilidId=6),
            Item(pinyin="liù qī bā jiǔ shí", hanzi="六七八九十", indoPron="liu chi ba cio syr", arti="Enam tujuh delapan sembilan sepuluh (6-10)", contoh="七天 = Tujuh hari (seminggu)", kategori="ANGKA", halamanId=40, jilidId=6),
            Item(pinyin="bǎi", hanzi="百", indoPron="bai (ba+i)", arti="Seratus (100)", contoh="一百 = Seratus", kategori="ANGKA", halamanId=40, jilidId=6),
            Item(pinyin="qiān", hanzi="千", indoPron="chien (ch+ien)", arti="Seribu (1000)", contoh="一千 = Seribu", kategori="ANGKA", halamanId=40, jilidId=6),
            Item(pinyin="duōshǎo qián", hanzi="多少钱", indoPron="dwo shao chien", arti="Berapa harganya?", contoh="这个多少钱？= Ini berapa?", kategori="ANGKA", halamanId=40, jilidId=6)
        )
        wordItems.forEach { db.itemDao().insertItem(it) }
    }

    private suspend fun populateJilid7(db: AppDatabase, kalimatList: List<RawItem>) {
        val halamanList = (42..45).mapIndexed { idx, id ->
            Halaman(id=id, jilidId=7, nomorHalaman=idx+1,
                judul=when(idx) {
                    0 -> "Sapaan & Perkenalan"
                    1 -> "Di Sekolah & Kantor"
                    2 -> "Belanja & Makanan"
                    else -> "Perjalanan & Umum"
                })
        }
        db.halamanDao().insertAllHalaman(halamanList)

        val halamanIds = listOf(42,42,42,42,42, 43,43,43,43,43, 44,44,44,44,44, 45,45,45,45,45)
        kalimatList.take(20).forEachIndexed { index, raw ->
            db.itemDao().insertItem(Item(
                pinyin = raw.pinyin,
                hanzi = raw.kalimat,
                indoPron = raw.indo_like,
                arti = raw.arti,
                kategori = "KALIMAT",
                halamanId = halamanIds.getOrElse(index) { 45 },
                jilidId = 7
            ))
        }
    }

    private suspend fun populateJilid8(db: AppDatabase, ceritaList: List<RawItem>) {
        val halamanList = (46..50).mapIndexed { idx, id ->
            Halaman(id=id, jilidId=8, nomorHalaman=idx+1,
                judul="Cerita ${idx+1}: ${ceritaList.getOrNull(idx)?.judul ?: "Cerita Pendek"}")
        }
        db.halamanDao().insertAllHalaman(halamanList)

        ceritaList.take(5).forEachIndexed { index, raw ->
            db.itemDao().insertItem(Item(
                pinyin = raw.pinyin ?: "",
                hanzi = raw.isi,
                indoPron = raw.indo_like,
                arti = raw.arti ?: "",
                contoh = raw.judul,
                kategori = "CERITA",
                halamanId = 46 + index,
                jilidId = 8
            ))
        }
    }

    private suspend fun populateJilid9to12(db: AppDatabase) {
        // Jilid 9: SRS Review Dasar
        val h9 = listOf(
            Halaman(id=51, jilidId=9, nomorHalaman=1, judul="Review Otomatis"),
            Halaman(id=52, jilidId=9, nomorHalaman=2, judul="Item yang sering salah"),
            Halaman(id=53, jilidId=9, nomorHalaman=3, judul="Latihan campuran")
        )
        db.halamanDao().insertAllHalaman(h9)
        db.itemDao().insertItem(Item(pinyin="[SRS Auto]", indoPron="Item review dipilih otomatis dari riwayat belajar Anda.", arti="Sistem akan menampilkan item yang perlu diulang berdasarkan algoritma Leitner.", kategori="SRS_INFO", halamanId=51, jilidId=9))

        // Jilid 10: SRS Advanced
        val h10 = listOf(
            Halaman(id=54, jilidId=10, nomorHalaman=1, judul="Long Interval Review"),
            Halaman(id=55, jilidId=10, nomorHalaman=2, judul="Mix Semua Jilid"),
            Halaman(id=56, jilidId=10, nomorHalaman=3, judul="Test Diri")
        )
        db.halamanDao().insertAllHalaman(h10)
        db.itemDao().insertItem(Item(pinyin="[SRS Advanced]", indoPron="Item dari semua jilid dicampur dengan interval panjang (14-30 hari).", arti="Review lanjutan untuk memperkuat memori jangka panjang.", kategori="SRS_INFO", halamanId=54, jilidId=10))

        // Jilid 11: Immersion Listening
        val h11 = listOf(
            Halaman(id=57, jilidId=11, nomorHalaman=1, judul="Dialog: Di Pasar"),
            Halaman(id=58, jilidId=11, nomorHalaman=2, judul="Dialog: Di Sekolah"),
            Halaman(id=59, jilidId=11, nomorHalaman=3, judul="Shadowing Practice"),
            Halaman(id=60, jilidId=11, nomorHalaman=4, judul="Free Listening")
        )
        db.halamanDao().insertAllHalaman(h11)
        val listeningItems = listOf(
            Item(pinyin="Nǐ yào mǎi shénme?", hanzi="你要买什么？", indoPron="ni yao mai sen me", arti="Kamu mau beli apa?", kategori="DIALOG", halamanId=57, jilidId=11),
            Item(pinyin="Wǒ yào mǎi píngguǒ.", hanzi="我要买苹果。", indoPron="wo yao mai ping gwo", arti="Saya mau beli apel.", kategori="DIALOG", halamanId=57, jilidId=11),
            Item(pinyin="Duōshǎo qián?", hanzi="多少钱？", indoPron="dwo shao chien", arti="Berapa harganya?", kategori="DIALOG", halamanId=57, jilidId=11),
            Item(pinyin="Wǔ kuài qián.", hanzi="五块钱。", indoPron="wu kwai chien", arti="Lima yuan.", kategori="DIALOG", halamanId=57, jilidId=11),
            Item(pinyin="Lǎoshī hǎo!", hanzi="老师好！", indoPron="lao syr hao", arti="Selamat datang, Guru! (Sapaan ke guru)", kategori="DIALOG", halamanId=58, jilidId=11),
            Item(pinyin="Tóngxuémen hǎo!", hanzi="同学们好！", indoPron="tong syue men hao", arti="Selamat datang, para siswa!", kategori="DIALOG", halamanId=58, jilidId=11)
        )
        listeningItems.forEach { db.itemDao().insertItem(it) }

        // Jilid 12: Custom / Import
        val h12 = listOf(
            Halaman(id=61, jilidId=12, nomorHalaman=1, judul="Kalimat Saya Sendiri"),
            Halaman(id=62, jilidId=12, nomorHalaman=2, judul="Vocab Personal")
        )
        db.halamanDao().insertAllHalaman(h12)
        db.itemDao().insertItem(Item(pinyin="[Tambah sendiri]", hanzi="自定义", indoPron="Tekan tombol ➕ untuk tambah item Anda sendiri!", arti="Gunakan fitur ini untuk menambah kalimat atau kata yang ingin kamu pelajari.", kategori="CUSTOM_INFO", halamanId=61, jilidId=12))
    }
}
