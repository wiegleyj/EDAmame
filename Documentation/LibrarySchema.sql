-- Symbols

CREATE TABLE version (
    'versionDate' TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    'versionMajor' TINYINT NOT NULL DEFAULT 1,
    'versionMinor' TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE symbols (
    'SymbolID' BINARY(16) NOT NULL PRIMARY KEY, -- every symbol gets a UUID.
    'Version' SMALLINT NOT NULL DEFAULT 1, -- symbols can be edited/improved. Versions are tracked by a number
    'Created' TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- original creation
    'Modified' TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- date of most recent version.
    'Author' VARCHAR(255), -- name of original author.
    'YAML' BLOB NOT NULL -- data for the graphics of the symbol. (serialization of java symbol object.
);

-- the symbol is stored as YAML in a blob. All aspects such as lines, arc, strings depicted in the symbol
-- are not stored as individual elements in the library because there is no need. Symbols objects are
-- instantiated in EDAmame by deserializing this YAML data.

