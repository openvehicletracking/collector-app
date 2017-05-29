# MotodevCollector App

Tcp uzerinden takip cihazlarindan gelen veriyi parse ederek veritabanina kaydeder. Kaydedilen mesajlar http uzeriden istemciler ile paylasilir.

## Gereksinimler

- java 1.8
- gradle 3.x
- mongodb 3.4

## Kurulum

Henuz net.motodev paketleri development asamasinda oldugundan oturu bagimliklarin local repoya install edilmesi gerekli. bunun icin __core__ ve __device-*-impl__ paketlerinin clonelanip yerel repoya yuklenmesi gereklidir.

#### core kutuphanesi icin

```
git clone https://github.com/motodevs/core.git
cd core/
gradle clean install
```

#### xtakip cihaz implemetanasyonu icin

```
git clone https://github.com/motodevs/device-xtakip-impl.git
cd codevice-xtakip-imple/
gradle clean install
```

komutlarini uygulayiniz.

#### uygulamayi clonelayin

```
git clone https://github.com/motodevs/collector-app.git
```

gelistirmeye hazirsiniz.


## Calistirma

`conf/config-dev.json` icindeki konfigurasyonu kendinize gore duzenleyiniz. `net.motodev.collector.AppMain.main` metodunu -Dconf=conf/config-dev.json paremetresi ile calistiriniz.

![figure1](https://i.hizliresim.com/r3q991.png "run1")


## Yardimci Uygulamalar

- [Packet Sender](https://packetsender.com/) tcp uzerinden paket gondermenize yardimci olur.
- [Test Ortami](http://tracker-test.motodev.net:10001/api/messages/test-device-id)'ndan rawMessage field'indaki deger ile data mesaj olusturabilirsiniz.

![figure2](https://i.hizliresim.com/O0WMy0.png "packetsender")

