import json

with open("fluid_tank.json") as f:
    raw = f.read()
    j = json.loads(raw)
    for entry in j.get("elements"):
        faces = entry["faces"]
        for key in faces:
            faces[key]["uv"] = [i*2 for i in faces[key]["uv"]]


    with open("fluid_tank_new.json", "w") as newf:
        newf.write(json.dumps(j, indent=4))
