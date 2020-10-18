package net.dankito.banking.ui.model.settings


interface ITanView {

    val didTanMethodSettingsChange: Boolean

    val tanMethodSettings: TanMethodSettings?

}