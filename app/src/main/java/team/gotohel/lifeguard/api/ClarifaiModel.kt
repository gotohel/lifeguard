package team.gotohel.lifeguard.api

class ClarifaiUploadModel(base64: String) {
    val inputs: List<Input> = listOf(Input(Data(Image(base64))))
}
class Input(val data: Data)
class Data(val image: Image)
class Image(val base64: String)

class ClarifaiResponseModel(val outputs: List<ClarifaiOutput>?)
class ClarifaiOutput(val data: ClarifaiOutputData?)
class ClarifaiOutputData(val concepts: List<Concept>?)
class Concept(
    val id: String?,
    val name: String?,
    val value: String?,
    val app_id: String?
)
