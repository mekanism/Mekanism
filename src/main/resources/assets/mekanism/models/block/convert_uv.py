import json

s = "pressurized_reaction_chamber.json"
with open(s) as f:
    raw = f.read()
    j = json.loads(raw)
    for entry in j.get("elements"):
        faces = entry["faces"]
        for key in faces:
            faces[key]["uv"] = [i*2 for i in faces[key]["uv"]]


    with open(s, "w") as newf:
        newf.write(json.dumps(j, indent=4))
