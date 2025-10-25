-- This file allows writing SQL commands that will be emitted in test and dev.
-- Initialization data for Budget Manager v2

-- Default French Banks
-- These will be automatically inserted when the application starts in dev/test mode
INSERT INTO banques (id, nom, couleur_theme, logo_url, actif) VALUES
(gen_random_uuid(), 'Crédit Agricole', '#00A651', '/logos/credit-agricole.png', true),
(gen_random_uuid(), 'BNP Paribas', '#00915A', '/logos/bnp-paribas.png', true),
(gen_random_uuid(), 'Société Générale', '#E20026', '/logos/societe-generale.png', true),
(gen_random_uuid(), 'Fortuneo', '#FF6600', '/logos/fortuneo.png', true),
(gen_random_uuid(), 'Boursorama', '#FF6900', '/logos/boursorama.png', true),
(gen_random_uuid(), 'Trade Republic', '#00D4AA', '/logos/trade-republic.png', true),
(gen_random_uuid(), 'Revolut', '#0075EB', '/logos/revolut.png', true),
(gen_random_uuid(), 'N26', '#00D4AA', '/logos/n26.png', true),
(gen_random_uuid(), 'Linxea', '#2E5BFF', '/logos/linxea.png', true),
(gen_random_uuid(), 'ING Direct', '#FF6200', '/logos/ing.png', true);