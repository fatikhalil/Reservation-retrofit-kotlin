package ma.ensa.reservationretrofitko.ui.chambres
import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ma.ensa.reservationretrofitko.R
import ma.ensa.reservationretrofitko.api.ApiClient
import ma.ensa.reservationretrofitko.api.ApiInterface
import ma.ensa.reservationretrofitko.models.Chambre
import ma.ensa.reservationretrofitko.models.DispoChambre
import ma.ensa.reservationretrofitko.models.TypeChambre
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson

class ChambreDetailActivity : AppCompatActivity() {

    private lateinit var spinnerTypeChambre: Spinner
    private lateinit var editPrix: EditText
    private lateinit var spinnerDispoChambre: Spinner
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var chambre: Chambre
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chambre_detail)

        // Initialisation des vues
        spinnerTypeChambre = findViewById(R.id.spinnerTypeChambre)
        editPrix = findViewById(R.id.editPrix)
        spinnerDispoChambre = findViewById(R.id.spinnerDispoChambre)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)

        // Configurer les Spinners
        setupSpinners()

        // Récupérer l'ID de la chambre depuis l'intent
        val chambreId = intent.getLongExtra("CHAMBRE_ID", -1)
        if (chambreId == -1L) {
            Toast.makeText(this, "Chambre introuvable", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            loadChambreDetails(chambreId)
        }

        // Bouton de mise à jour
        btnUpdate.setOnClickListener { updateChambre() }

        // Bouton de suppression
        btnDelete.setOnClickListener { deleteChambre() }
    }

    private fun setupSpinners() {
        // Adapter pour TypeChambre
        val typeChambreAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, TypeChambre.values()
        )
        typeChambreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTypeChambre.adapter = typeChambreAdapter

        // Adapter pour DispoChambre
        val dispoChambreAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, DispoChambre.values()
        )
        dispoChambreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDispoChambre.adapter = dispoChambreAdapter
    }

    private fun loadChambreDetails(chambreId: Long) {
        // Temps de début
        val startTime = System.currentTimeMillis()

        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val call = apiService.getChambreById(chambreId)
        call.enqueue(object : Callback<Chambre> {
            override fun onResponse(call: Call<Chambre>, response: Response<Chambre>) {
                // Temps de fin
                val endTime = System.currentTimeMillis()
                val durationMs = endTime - startTime // Temps écoulé en millisecondes

                if (response.isSuccessful && response.body() != null) {
                    chambre = response.body()!!

                    // Mesurer la taille des données reçues
                    val jsonString = gson.toJson(chambre)
                    val sizeInBytes = jsonString.toByteArray().size // Taille en octets
                    val sizeInKB = sizeInBytes / 1024.0 // Convertir en KB

                    // Afficher les résultats dans les logs
                    println("Taille des données reçues (GET) : $sizeInKB KB")
                    println("Temps de réponse (GET) : $durationMs ms")

                    // Vérifier si les propriétés sont non null avant de les utiliser
                    chambre.id?.let { id ->
                        spinnerTypeChambre.setSelection(TypeChambre.values().indexOf(chambre.typeChambre))
                        editPrix.setText(chambre.prix.toString())
                        spinnerDispoChambre.setSelection(DispoChambre.values().indexOf(chambre.dispoChambre))
                    } ?: run {
                        Toast.makeText(this@ChambreDetailActivity, "ID de chambre invalide", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@ChambreDetailActivity, "Chambre introuvable", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<Chambre>, t: Throwable) {
                // Temps de fin en cas d'échec
                val endTime = System.currentTimeMillis()
                val durationMs = endTime - startTime // Temps écoulé en millisecondes

                // Afficher les résultats dans les logs
                println("Temps de réponse (GET - échec) : $durationMs ms")

                Toast.makeText(this@ChambreDetailActivity, "Erreur de connexion", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun updateChambre() {
        val prixStr = editPrix.text.toString()

        if (prixStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            chambre.apply {
                typeChambre = spinnerTypeChambre.selectedItem as TypeChambre
                prix = prixStr.toDouble()
                dispoChambre = spinnerDispoChambre.selectedItem as DispoChambre
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Prix invalide", Toast.LENGTH_SHORT).show()
            return
        }

        // Vérifier si l'ID de la chambre est non null
        chambre.id?.let { id ->
            // Temps de début
            val startTime = System.currentTimeMillis()

            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.updateChambre(id, chambre)
            call.enqueue(object : Callback<Chambre> {
                override fun onResponse(call: Call<Chambre>, response: Response<Chambre>) {
                    // Temps de fin
                    val endTime = System.currentTimeMillis()
                    val durationMs = endTime - startTime // Temps écoulé en millisecondes

                    // Mesurer la taille des données reçues
                    val jsonString = response.body()?.let { gson.toJson(it) } ?: ""
                    val sizeInBytes = jsonString.toByteArray().size // Taille en octets
                    val sizeInKB = sizeInBytes / 1024.0 // Convertir en KB

                    // Afficher les résultats dans les logs
                    println("Taille des données reçues (PUT) : $sizeInKB KB")
                    println("Temps de réponse (PUT) : $durationMs ms")

                    if (response.isSuccessful) {
                        Toast.makeText(this@ChambreDetailActivity, "Chambre mise à jour", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@ChambreDetailActivity, "Erreur de mise à jour", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Chambre>, t: Throwable) {
                    // Temps de fin en cas d'échec
                    val endTime = System.currentTimeMillis()
                    val durationMs = endTime - startTime // Temps écoulé en millisecondes

                    // Afficher les résultats dans les logs
                    println("Temps de réponse (PUT - échec) : $durationMs ms")

                    Toast.makeText(this@ChambreDetailActivity, "Erreur de connexion", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            Toast.makeText(this, "ID de chambre invalide", Toast.LENGTH_SHORT).show()
        }
    }
    private fun deleteChambre() {
        AlertDialog.Builder(this)
            .setTitle("Supprimer la chambre")
            .setMessage("Êtes-vous sûr de vouloir supprimer cette chambre ?")
            .setPositiveButton("Oui") { dialog, which ->
                // Vérifier si l'ID de la chambre est non null
                chambre.id?.let { id ->
                    // Temps de début
                    val startTime = System.currentTimeMillis()

                    val apiService = ApiClient.getClient().create(ApiInterface::class.java)
                    val call = apiService.deleteChambre(id)
                    call.enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            // Temps de fin
                            val endTime = System.currentTimeMillis()
                            val durationMs = endTime - startTime // Temps écoulé en millisecondes

                            // Mesurer la taille des données reçues
                            val sizeInBytes = response.raw().body?.contentLength() ?: 0 // Taille en octets
                            val sizeInKB = sizeInBytes / 1024.0 // Convertir en KB

                            // Afficher les résultats dans les logs
                            println("Taille des données reçues (DELETE) : $sizeInKB KB")
                            println("Temps de réponse (DELETE) : $durationMs ms")

                            if (response.isSuccessful) {
                                Toast.makeText(this@ChambreDetailActivity, "Chambre supprimée", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@ChambreDetailActivity,
                                    "La chambre est réservée et ne peut pas être supprimée.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            // Temps de fin en cas d'échec
                            val endTime = System.currentTimeMillis()
                            val durationMs = endTime - startTime // Temps écoulé en millisecondes

                            // Afficher les résultats dans les logs
                            println("Temps de réponse (DELETE - échec) : $durationMs ms")

                            Toast.makeText(this@ChambreDetailActivity, "Erreur de connexion", Toast.LENGTH_SHORT).show()
                        }
                    })
                } ?: run {
                    Toast.makeText(this, "ID de chambre invalide", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }
}