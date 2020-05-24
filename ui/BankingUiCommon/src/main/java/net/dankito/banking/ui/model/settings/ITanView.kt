package net.dankito.banking.ui.model.settings


interface ITanView {

    val didTanProcedureSettingsChange: Boolean

    val tanProcedureSettings: TanProcedureSettings?

}