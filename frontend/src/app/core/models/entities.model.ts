/** Generic Spring-Data page envelope. */
export interface Page<T> {
  content:       T[];
  totalElements: number;
  totalPages:    number;
  number:        number;
  size:          number;
  first:         boolean;
  last:          boolean;
}

export interface Employe {
  id:             number;
  nom:            string;
  prenom:         string;
  email:          string;
  posteLibelle?:  string;
  posteId?:       number;
  posteTitle?:    string;
  recrutementId?: number;
  userId?:        number;
  createdAt?:     string;
  updatedAt?:     string;
}

export interface Poste {
  id:                   number;
  titre:                string;
  competencesRequises?: string;
  competenceIds?:       number[];
  competenceNoms?:      string[];
  createdAt?:           string;
  updatedAt?:           string;
}

export interface Competence {
  id:         number;
  nom:        string;
  niveau?:    string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Recrutement {
  id:              number;
  posteCible:      string;
  statut:          string;   // OUVERT | EN_COURS | CLOS | EMBAUCHE
  responsableId?:  number;
  responsableNom?: string;
  createdAt?:      string;
  updatedAt?:      string;
}

export interface Candidate {
  id:                     number;
  nom:                    string;
  prenom:                 string;
  email:                  string;
  recrutementId?:         number;
  recrutementPosteCible?: string;
  createdAt?:             string;
  updatedAt?:             string;
}

export interface Conge {
  id:                  number;
  employeId?:          number;
  employeNomComplet?:  string;
  type:                string;
  dateDebut:           string;
  dateFin:             string;
  statut:              'PENDING' | 'APPROVED' | 'REJECTED';
  requestedAt?:        string;
  decidedAt?:          string;
  decidedByUserId?:    number;
  createdAt?:          string;
  updatedAt?:          string;
}

export interface Paie {
  id:                  number;
  employeId?:          number;
  employeNomComplet?:  string;
  montant:             number;
  mois:                number;
  annee:               number;
  bulletinPdf?:        string;
  createdAt?:          string;
  updatedAt?:          string;
}

export interface Contrat {
  id:                  number;
  type:                string;   // CDI | CDD | STAGE | ALTERNANCE
  dateDebut:           string;
  dateFin?:            string;
  salaire:             number;
  employeId?:          number;
  employeNomComplet?:  string;
  createdAt?:          string;
  updatedAt?:          string;
}

export interface DossierRH {
  id:                  number;
  infosPerso:          string;
  diplomes:            string;
  documents:           string;
  employeId?:          number;
  employeNomComplet?:  string;
  createdAt?:          string;
  updatedAt?:          string;
}

export interface Planning {
  id:          number;
  horaires?:   string;
  dateDebut?:  string;
  dateFin?:    string;
  type?:       string;
  employeId?:  number;
  employeNomComplet?: string;
  createdAt?:  string;
  updatedAt?:  string;
}

export interface Evaluation {
  id:                  number;
  objectifs:           string;
  kpi:                 string;
  score?:              number;
  dateEvaluation?:     string;
  employeId?:          number;
  employeNomComplet?:  string;
  createdAt?:          string;
  updatedAt?:          string;
}

export interface Formation {
  id:                  number;
  titre:               string;
  certification:       string;
  organisme?:          string;
  dateDebut?:          string;
  dateFin?:            string;
  employeId?:          number;
  employeNomComplet?:  string;
  createdAt?:          string;
  updatedAt?:          string;
}

export interface ResponsableRH {
  id:      number;
  nom:     string;
  userId?: number;
}
