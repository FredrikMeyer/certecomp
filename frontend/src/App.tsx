import React from "react";
import { AppBar, Box, Button, Container, TextField } from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "react-query";
import "./App.css";

import { z, ZodError } from "zod";

const ExerciseType = z.object({ id: z.number(), name: z.string().min(1) });
const ExerciseTypes = z.array(ExerciseType);

type ExerciseType = z.infer<typeof ExerciseType>;

async function getTypes(): Promise<ExerciseType[]> {
  const res = await fetch("http://127.0.0.1:3000/api/types");
  const jsonObj = await res.json();
  return ExerciseTypes.parse(jsonObj);
}

async function newType(name: string) {
  const res = await fetch("http://127.0.0.1:3000/api/types", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ name }),
  });
  if (!res.ok) {
    throw new Error("failure");
  }
}

function NewType() {
  const queryClient = useQueryClient();
  const mutation = useMutation({
    mutationFn: (name: string) => newType(name),
    onError: (e) => console.log("caught error", e),
    onSuccess: () => {
      queryClient.invalidateQueries("types");
    },
  });

  const [value, setValue] = React.useState("name");

  const validation = React.useMemo(
    () => ExerciseType.shape.name.safeParse(value),
    [value]
  );

    console.log(validation.success)

  return (
    <Box>
      {" "}
      <TextField
        required
        label="Type"
        value={value}
          error={!validation.success}
          helperText={!validation.success && validation.error.issues[0].message}
        onChange={(e) => setValue(e.target.value)}
      />
      <Button onClick={() => mutation.mutate(value)}>Submit</Button>
    </Box>
  );
}

function App() {
  const { data, isLoading, error } = useQuery<
    ExerciseType[],
    ZodError<ExerciseType[]>
  >("types", getTypes);

  return (
    <Container maxWidth={false}>
      <AppBar>Certe Comp</AppBar>
      <Box>
        {data ? data.map((el) => <div key={el.id}>{el.name}</div>) : "Loading"}
      </Box>
      <NewType />
    </Container>
  );
}

export default App;
