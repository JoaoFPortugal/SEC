#!/bin/bash

rm notary6066.db
rm notary6067.db
rm notary6068.db
rm notary6069.db
rm notary6070.db

echo -e ".read create.sql\n.read populate.sql\n.quit\n" | sqlite3 notary6066.db
echo -e ".read create.sql\n.read populate.sql\n.quit\n" | sqlite3 notary6067.db
echo -e ".read create.sql\n.read populate.sql\n.quit\n" | sqlite3 notary6068.db
echo -e ".read create.sql\n.read populate.sql\n.quit\n" | sqlite3 notary6069.db
echo -e ".read create.sql\n.read populate.sql\n.quit\n" | sqlite3 notary6070.db
