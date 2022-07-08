-- Symbols

CREATE TABLE symbols (
    SymbolID BINARY(16), -- every symbol gets a UUID.
    VersionMajor TINYINT, -- symbols can be edited/improved. Versions are tracked by a major number
    VersionMinor TINYINT, -- versions also have a minor number.
    Created TIMESTAMP, -- original creation
    Modified TIMESTAMP -- date of most recent version.
    Author VARCHAR(255), -- name of original author.
    JSON BLOB -- data for the graphics of the symbol. (serialization of java symbol object.
);

-- the symbol is stored as JSON in a blob. All aspects such as lines, arc, strings depicted in the symbol
-- are not stored as individual elements in the library because there is no need. Symbols objects are
-- instantiated in EDAmame by deserializing this JSON data.

