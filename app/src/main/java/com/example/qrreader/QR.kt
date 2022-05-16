package com.example.qrreader

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.net.MalformedURLException
import java.net.URL

class QR : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private val PERMISOCAMARA = 1
    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerView = ZXingScannerView(this)
        setContentView(scannerView)

        if (checkPerm()){
            //se concedió el permiso
        }else{
            askPerm()
        }

        scannerView?.setResultHandler(this)
        scannerView?.startCamera()
    }

    private fun askPerm() {
        ActivityCompat.requestPermissions(this@QR, arrayOf(Manifest.permission.CAMERA),PERMISOCAMARA)
    }

    private fun checkPerm(): Boolean {
        return (ContextCompat.checkSelfPermission(this@QR, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    override fun handleResult(p0: Result?) {
        //Código QR leído
        val scanResult = p0?.text
        Log.d("QRLEIDO", scanResult!!)

        when {
            scanResult.startsWith("https://") -> {
                Log.d("QRLEIDO", "URL")
                actURL(scanResult)
            }
            scanResult.startsWith("MATMSG") || scanResult.startsWith("mail")-> {
                Log.d("QRLEIDO", "CORREO")
                actMail(scanResult)

            }
            scanResult.startsWith("SMS") || scanResult.startsWith("sms")-> {
                Log.d("QRLEIDO", "MENSAJE")
                actMSG(scanResult)
            }
            scanResult.startsWith("BEGIN:VCARD") -> {
                Log.d("QRLEIDO", "VCARD")
                actVCard(scanResult)
            }
            else -> error()
        }

    }



    fun actURL (data:String){
        val url = URL(data)
        val i = Intent(Intent.ACTION_VIEW)
        i.setData(Uri.parse(data))
        startActivity(i)
        finish()
    }

    fun actMail (data: String){
        var auxC = data.indexOf("TO:")
        var auxfC = data.indexOf(";SUB:", auxC+3)
        var correo = ""
        for (i in auxC+3 until auxfC) correo += (data[i])
        Log.d("QRLEIDO", correo)

        var auxfS = data.indexOf(";BODY:", auxfC+5)
        var sub = ""
        for (i in auxfC+5 until auxfS) sub += (data[i])
        Log.d("QRLEIDO", sub)

        var auxfB = data.indexOf(";;", auxfS+6)
        var body = ""
        for (i in auxfS+6 until auxfB) body += (data[i])
        Log.d("QRLEIDO", body)

        var intent = Intent(Intent.ACTION_SENDTO)
        var emails = arrayOf(correo)
        intent.setType("*/*")
        intent.setData(Uri.parse("mailto:"))
        intent.putExtra(Intent.EXTRA_EMAIL, emails)
        intent.putExtra(Intent.EXTRA_SUBJECT, sub)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        startActivity(intent)
        finish()
    }

    fun actMSG (data: String){
        var auxN = data.indexOf("TO:")
        var auxfN = data.indexOf(":",auxN+3)
        var numero = ""
        for (i in auxN+3 until auxfN) numero += (data[i])
        Log.d("QRLEIDO", numero)
        var text = ""
        for (i in auxfN+1 until data.length) text += (data[i])
        Log.d("QRLEIDO", text)

        var intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", numero, null))
        intent.putExtra("sms_body",text)
        startActivity(intent)
        finish()
    }

    fun actVCard (data: String){
        //version
        var auxV = data.indexOf("VERSION:")
        var auxfV= data.indexOf("N:",auxV+8)
        var version = ""
        for (i in auxV+8 until auxfV) version += (data[i])
        Log.d("QRLEIDO", version)
        //n
        var auxfN = data.indexOf("FN:", auxfV+2)
        var n = ""
        for (i in auxfV+2 until auxfN) n += (data[i])
        var listn = n.split(";")
        var name = ""
        name += listn[0] +  " " + listn[1]
        Log.d("QRLEIDO", name)
        //FN
        var auxfFN = data.indexOf("ORG:", auxfN+2)
        var fn = ""
        for (i in auxfN+3 until auxfFN) fn += (data[i])
        Log.d("QRLEIDO", fn)
        //ORG
        var auxfORG = data.indexOf("TITLE:", auxfFN+4)
        var org = ""
        for (i in auxfFN+4 until auxfORG) org += (data[i])
        Log.d("QRLEIDO", "org:$org")
        //TITLE
        var auxftitle = data.indexOf("ADR:", auxfORG+6)
        var title = ""
        for (i in auxfORG+6 until auxftitle) title += (data[i])
        Log.d("QRLEIDO", "title:$title")
        //ADR
        var auxfadr = data.indexOf("TEL;WORK;VOICE:", auxftitle+4)
        var adr = ""
        for (i in auxftitle+4 until auxfadr) adr += (data[i])
        Log.d("QRLEIDO", "adr:$adr")
        //TELWORK
        var auxftelwk = data.indexOf("TEL;CELL:", auxfadr+15)
        var telw = ""
        for (i in auxfadr+15 until auxftelwk) telw += (data[i])
        Log.d("QRLEIDO", "telw:$telw")
        //TELCELL
        var auxftelcll = data.indexOf("TEL;FAX:", auxftelwk+9)
        var telcell = ""
        for (i in auxftelwk+9 until auxftelcll) telcell += (data[i])
        Log.d("QRLEIDO", "telcell:$telcell")
        //TELFAX
        var auxftelfax = data.indexOf("EMAIL;WORK;INTERNET:", auxftelcll+8)
        var telfax = ""
        for (i in auxftelcll+8 until auxftelfax) telfax += (data[i])
        Log.d("QRLEIDO", "telfax:$telfax")
        //EMAIL
        var auxfemail = data.indexOf("URL:", auxftelfax+20)
        var email = ""
        for (i in auxftelfax+20 until auxfemail) email += (data[i])
        Log.d("QRLEIDO", "email:$email")
        //URL
        var auxfurl = data.indexOf("END:", auxfemail+4)
        var url = ""
        for (i in auxfemail+4 until auxfurl) url += (data[i])
        Log.d("QRLEIDO", "url:$url")

        //url = "https://akjskdlfjas.com"
        //org = "patitos"
        //title = "jefe"
        //telw = "511111111"
        //telfax = "81111111"
        var intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, name)
            putExtra(ContactsContract.Intents.Insert.PHONETIC_NAME, fn)
            putExtra(ContactsContract.Intents.Insert.COMPANY, org)
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, title)
            putExtra(ContactsContract.Intents.Insert.POSTAL, adr)
            putExtra(ContactsContract.Intents.Insert.PHONE, telcell)
            putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, telw)
            putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, telfax)
            putExtra(ContactsContract.Intents.Insert.EMAIL, email)
        }
        startActivity(intent)
        finish()
    }

    fun error () {
        AlertDialog.Builder(this@QR)
            .setTitle(getString(R.string.error))
            .setMessage(getString(R.string.errormsg))
            .setPositiveButton(getString(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
                finish()
            })
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        if(checkPerm()){
            if(scannerView == null){
                scannerView = ZXingScannerView(this)
                setContentView(scannerView)
            }
            scannerView?.setResultHandler(this)
            scannerView?.startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerView?.stopCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISOCAMARA -> {
                if(grantResults.isNotEmpty() && grantResults[0]!=PackageManager.PERMISSION_GRANTED){ //Si ya recibi resultado y no se concedio
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        AlertDialog.Builder(this@QR)
                            .setTitle(getString(R.string.askperm))
                            .setMessage(getString(R.string.msgperm))
                            .setPositiveButton(getString(R.string.aceptar), DialogInterface.OnClickListener{ dialogInterface, i ->
                                requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISOCAMARA)
                            })
                            .setNegativeButton(getString(R.string.cancelar), DialogInterface.OnClickListener { dialogInterface, i ->
                                dialogInterface.dismiss()
                                finish()
                            })
                            .create()
                            .show()
                    }else{
                        Toast.makeText(this@QR, getString(R.string.sinperm), Toast.LENGTH_LONG).show()
                        //ahi veo si lo dejo
                        var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //abre una nueva activity
                        var uri = Uri.fromParts("package", packageName, null)
                        intent.setData(uri)
                        startActivity(intent)
                        //fin ahi veo si lo dejo
                        finish()
                    }
                }
            }
        }

    }

}