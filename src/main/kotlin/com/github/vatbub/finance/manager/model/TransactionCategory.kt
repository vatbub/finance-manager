/*-
 * #%L
 * finance-manager
 * %%
 * Copyright (C) 2019 - 2021 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.finance.manager.model

import com.github.vatbub.finance.manager.model.TransactionCategory.*

enum class TransactionCategory {
    Food, Unknown, Electronics, Rent, Transport, Entertainment, Insurances, Salary, Medical, Phone, OtherShopping,
    Fees, Renovation, Gifts, Transfer, Beauty, Clothing, Sport, Hobbies, Furniture, Holidays, Garden, Communication,
    Education, GovernmentalSupport, Interest
}

fun String.toTransactionCategory(): TransactionCategory = when (this) {
    "Lebensmittel" -> Food
    "Restaurants & Cafes" -> Food
    "Essen & Trinken (Sonstiges)" -> Food
    "Unkategorisierte Ausgaben" -> Unknown
    "Unkategorisierte Einnahmen" -> Unknown
    "Einnahmen (Sonstiges)" -> Unknown
    "Elektronik & Computer" -> Electronics
    "Miete" -> Rent
    "Öffentliche Verkehrsmittel & Taxi" -> Transport
    "Flüge, Autos & Beförderung" -> Transport
    "Treibstoff" -> Transport
    "Musik, Filme & Apps" -> Entertainment
    "Computer- und Videospiele" -> Entertainment
    "Bücher, Zeitschriften & Spiele" -> Entertainment
    "Musik & Instrumente" -> Entertainment
    "Versicherungen (Sonstiges)" -> Insurances
    "Gehalt & Lohn" -> Salary
    "Arzneimittel & Medizinprodukte" -> Medical
    "Gesundheit & Pflege (Sonstiges)" -> Medical
    "Ärzte & Krankenhaus" -> Medical
    "Mobilfunk" -> Phone
    "Einkäufe & Dienstleistungen (Sonstiges)" -> OtherShopping
    "Gebühren & Zinsen" -> Fees
    "Steuern" -> Fees
    "Renovierung & Instandhaltung" -> Renovation
    "Geschenke & Spenden" -> Gifts
    "Geldautomat & Barauszahlung" -> Transfer
    "Umbuchung zwischen Konten " -> Transfer
    "Friseur & Körperpflege" -> Beauty
    "Kleidung & Schuhe" -> Clothing
    "Freizeit & Sport (Sonstiges)" -> Sport
    "Haushaltsgeräte & Einrichtung" -> Furniture
    "Vereine & Mitgliedschaften" -> Hobbies
    "Kfz: Stellplatz" -> Transport
    "Mietwagen & Carsharing" -> Transport
    "Kfz: Finanzierung" -> Transport
    "Krankenversicherung" -> Insurances
    "Zusatzversicherungen" -> Insurances
    "Spielzeug" -> Hobbies
    "Kino, Theater & Events" -> Hobbies
    "Hotels & Unterkunft" -> Holidays
    "Kommunikation & Unterhaltung" -> Communication
    "Unterricht, Studiengebühren & Kurse" -> Education
    "Staatliche Hilfe" -> GovernmentalSupport
    "Garten & Außenanlagen" -> Garden
    "Zins- & Kapitalerträge" -> Interest
    "Workout & Fitness" -> Sport
    "Zubuchung" -> Transfer
    "Bars, Clubs & Nachtleben" -> Hobbies
    "Ausbildung" -> Education
    else -> valueOf(this)
}
